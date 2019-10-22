package com.xxq.sdk.processor;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftStruct;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.*;
import com.xxq.domain.common.pagination.Paged;
import com.xxq.sdk.processor.annotation.ThriftPaged;
import org.springframework.data.domain.Page;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
public class ThriftPagedProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        try {
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(ThriftPaged.class)) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                process(typeElement);
            }
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "ThriftPaged生成异常：" + e.getMessage());
        }
        return true;
    }


    private void process(TypeElement typeElement) throws IOException {
        PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
        ClassName originName = ClassName.get(typeElement);
        String simpleName = "Paged" + typeElement.getSimpleName();
        String packageName = packageElement.getQualifiedName() + ".pagination";
        ClassName className = ClassName.get(packageName, simpleName);
        TypeSpec.Builder builder = TypeSpec.classBuilder(simpleName);

        builder.superclass(ParameterizedTypeName.get(ClassName.get(Paged.class), originName));
        builder.addModifiers(Modifier.PUBLIC).addAnnotation(ThriftStruct.class);

        MethodSpec constructor1 = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ThriftConstructor.class)
                .addParameter(long.class, "total")
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), originName), "data")
                .addStatement("super(total,data)")
                .build();

        MethodSpec constructor2 = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Page.class), originName), "page")
                .addStatement("super(page)")
                .build();

        builder.addMethod(constructor1).addMethod(constructor2);

        TypeSpec targetType = builder.build();

        JavaFile sourceCode = JavaFile.builder(packageName, targetType).build();
        Writer sourceFileWriter = processingEnv.getFiler()
                .createSourceFile(className.toString())
                .openWriter();
        sourceCode.writeTo(sourceFileWriter);
        sourceFileWriter.close();

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(ThriftPaged.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
