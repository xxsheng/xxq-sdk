package com.xxq.sdk.processor;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.*;
import com.xxq.sdk.processor.annotation.EnumSetConverter;

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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class EnumSetJpaConverterProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        try {
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(EnumSetConverter.class)) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                process(typeElement);
            }
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "jpa converter生成异常：" + e.getMessage());
        }
        return true;
    }


    private void process(TypeElement typeElement) throws IOException {
        PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
        ClassName enumName = ClassName.get(typeElement);
        ParameterizedTypeName convertType = ParameterizedTypeName.get(ClassName.get(Set.class),enumName);
        String simpleName = typeElement.getSimpleName() + "EnumSetConverter";
        String packageName = packageElement.getQualifiedName() + ".converter";

        ClassName className = ClassName.get(packageName, simpleName);
        TypeSpec.Builder builder = TypeSpec.classBuilder(simpleName);
        builder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(AttributeConverter.class), convertType, ClassName.get(String.class)));
        builder.addModifiers(Modifier.PUBLIC);
        builder.addMethod(MethodSpec.methodBuilder("convertToDatabaseColumn")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addParameter(convertType, "attribute")
                .addAnnotation(Override.class)
                .addCode(CodeBlock.builder()
                        .add("if (attribute != null) {" +
                                " return attribute.stream().map(Enum::name).collect($T.joining(\",\"));\n" +
                                "}\n" +
                                "return null;", Collectors.class)
                        .build())
                .build());
        builder.addMethod(MethodSpec.methodBuilder("convertToEntityAttribute")
                .addModifiers(Modifier.PUBLIC)
                .returns(convertType)
                .addParameter(ParameterSpec.builder(String.class, "dbData").build())
                .addAnnotation(Override.class)
                .addCode(CodeBlock.builder()
                        .add("if (dbData != null) {" +
                                "$T set= $T.noneOf($T.class);\n"+
                                "$T.stream(dbData.split(\",\")).map($T::valueOf).forEach(set::add);\n"+
                                "return set;\n" +
                                "}\n"+
                                "return null;", convertType, EnumSet.class, enumName, Arrays.class, enumName)
                        .build())
                .build());
        builder.addAnnotation(AnnotationSpec.builder(Converter.class).addMember("autoApply", "$L", true).build());
        TypeSpec enumConverter = builder.build();

        JavaFile sourceCode = JavaFile.builder(packageName, enumConverter).build();
        Writer sourceFileWriter = processingEnv.getFiler()
                .createSourceFile(className.toString())
                .openWriter();
        sourceCode.writeTo(sourceFileWriter);
        sourceFileWriter.close();

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(EnumSetConverter.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
