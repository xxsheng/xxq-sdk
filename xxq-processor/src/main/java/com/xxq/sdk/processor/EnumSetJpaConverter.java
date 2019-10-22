package com.xxq.sdk.processor;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.*;
import com.xxq.sdk.processor.annotation.JpaConverter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@AutoService(Processor.class)
public class EnumSetJpaConverter extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Messager messager = processingEnv.getMessager();

        try {
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(JpaConverter.class)) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                process(typeElement);
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "jpa converter 生成异常：" + e.getMessage());
        }
        return false;

    }

    private void process(TypeElement typeElement) throws IOException {
        PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
        ClassName originName = ClassName.get(typeElement);

        String simpleName = typeElement.getSimpleName() + "JpaConverter";
        String packageName = packageElement.getQualifiedName() + ".converter";

        ClassName className = ClassName.get(packageName, simpleName);
        TypeSpec.Builder builder = TypeSpec.classBuilder(simpleName);
        builder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(AttributeConverter.class), originName, ClassName.get(String.class)));
        builder.addModifiers(Modifier.PUBLIC);
        builder.addMethod(MethodSpec.methodBuilder("converterToDatabaseColumn")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addParameter(ParameterSpec.builder(originName, "attribute").build())
                .addAnnotation(Override.class)
                .addCode(CodeBlock.builder().add("if (attribute != null) { return attribute.name();}return null;").build())
                .build());
        builder.addMethod(MethodSpec.methodBuilder("converterToEntityAttribute")
                .addModifiers(Modifier.PUBLIC)
                .returns(originName)
                .addParameter(ParameterSpec.builder(String.class, "dbData").build())
                .addAnnotation(Override.class)
                .addCode(CodeBlock.builder().add("if (dbData != null) {return $T.valueOf(dbData);}return null;", originName).build())
                .build());

        builder.addAnnotation(AnnotationSpec.builder(Converter.class).addMember("autoApply", "&L", true).build());
        TypeSpec enumConverterbuild = builder.build();

        JavaFile sourceCode = JavaFile.builder(packageName, enumConverterbuild).build();
        Writer sourceFileWriter = processingEnv.getFiler().createSourceFile(className.toString()).openWriter();
        sourceCode.writeTo(sourceFileWriter);
        sourceFileWriter.close();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(JpaConverter.class.getName(), com.xxq.sdk.processor.annotation.JpaConverter.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
