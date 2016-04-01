/*
 * Copyright 2016 BiaoWu

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package auto.http.internal.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.lang.reflect.Type;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;

import auto.http.internal.AutoHttpPlugins;
import auto.http.internal.ResultFactory;
import auto.http.internal.Utils;
import okhttp3.Request;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * @author biaowu
 */
final class AutoHttpWriter {
  private static final String FIELD_PLUGINS = "plugins";
  private final AutoHttpDescriptor autoHttpDescriptor;

  protected final ClassName name;
  private TypeSpec.Builder autoHttpClass;

  private final Messager messager;

  AutoHttpWriter(
      AutoHttpDescriptor autoHttpDescriptor,
      ClassName name,
      Messager messager) {
    this.autoHttpDescriptor = autoHttpDescriptor;
    this.name = name;
    this.messager = messager;
  }

  TypeSpec.Builder write() {
    autoHttpClass = createAutoHttpClass();

    buildField();
    buildConstructor();
    buildMethods();

    return autoHttpClass;
  }

  private void buildField() {
    autoHttpClass.addField(
        FieldSpec.builder(AutoHttpPlugins.class, FIELD_PLUGINS, PRIVATE)
            .build()
    );
  }

  private void buildConstructor() {
    autoHttpClass.addMethod(
        MethodSpec.constructorBuilder()
            .addModifiers(PUBLIC)
            .addParameter(AutoHttpPlugins.class, FIELD_PLUGINS)
            .addStatement("if ($N == null) throw new $T($S)",
                FIELD_PLUGINS,
                IllegalArgumentException.class,
                String.format("%s can not be null!", FIELD_PLUGINS))
            .addStatement("this.$N=$N", FIELD_PLUGINS, FIELD_PLUGINS)
            .build()
    );
  }

  private TypeSpec.Builder createAutoHttpClass() {
    return classBuilder(name.simpleName())
        .addModifiers(PUBLIC)
        .addSuperinterface(ClassName.get(autoHttpDescriptor.autoHttpElement()));
  }

  private void buildMethods() {
    for (AutoHttpDescriptor.MethodDescriptor methodDescriptor :
        autoHttpDescriptor.methodElements()) {
      ExecutableElement methodElement = methodDescriptor.methodElement();
      MethodSpec.Builder methodBuilder =
          MethodSpec.methodBuilder(methodElement.getSimpleName().toString())
              .addAnnotation(Override.class)
              .addModifiers(
                  Sets.difference(methodElement.getModifiers(), ImmutableSet.of(ABSTRACT)))
              .returns(methodDescriptor.returnTypeName());

      String returnType = "returnType";
      StringBuilder returnTypeFormat =
          new StringBuilder("$T $N = $T.wrapType($N.class,");
      ImmutableList<Class<?>> returnTypeArguments = methodDescriptor.returnTypeArguments();
      for (int i = 0; i < returnTypeArguments.size(); i++) {
        returnTypeFormat.append(returnTypeArguments.get(i).getSimpleName());
        returnTypeFormat.append(".class,");
      }
      returnTypeFormat.deleteCharAt(returnTypeFormat.length() - 1).append(")");
      methodBuilder.addStatement(returnTypeFormat.toString(),
          Type.class,
          returnType,
          Utils.class,
          methodDescriptor.returnType().getSimpleName());


      String convertType = "convertType";
      StringBuilder convertTypeFormat =
          new StringBuilder("$T $N = $T.wrapType($N.class,");
      ImmutableList<Class<?>> converterTypeArguments = methodDescriptor.converterTypeArguments();
      for (int i = 0; i < converterTypeArguments.size(); i++) {
        convertTypeFormat.append(converterTypeArguments.get(i).getSimpleName());
        convertTypeFormat.append(".class,");
      }
      convertTypeFormat.deleteCharAt(convertTypeFormat.length() - 1).append(")");
      methodBuilder.addStatement(convertTypeFormat.toString(),
          Type.class,
          convertType,
          Utils.class,
          methodDescriptor.converterType().getSimpleName());

      //TODO method body impl
      String builder = "builder";

      methodBuilder
          .addStatement("$T $N = new $T()",
              Request.Builder.class,
              builder,
              Request.Builder.class)
          .addStatement("$N.url($N.baseUrl())", builder, FIELD_PLUGINS)
          .addStatement("return $T.result($N.build(),$N,$N,$N)",
              ResultFactory.class,
              builder,
              FIELD_PLUGINS,
              returnType,
              convertType);

      autoHttpClass.addMethod(methodBuilder.build());
    }
  }
}
