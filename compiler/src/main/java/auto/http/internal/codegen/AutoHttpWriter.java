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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * @author biaowu
 */
final class AutoHttpWriter {
  private final AutoHttpDescriptor autoHttpDescriptor;
  protected final ClassName name;

  private TypeSpec.Builder autoHttpClass;

  AutoHttpWriter(
      AutoHttpDescriptor autoHttpDescriptor,
      ClassName name) {
    this.autoHttpDescriptor = autoHttpDescriptor;
    this.name = name;
  }

  public TypeSpec.Builder write() {
    autoHttpClass = createAutoHttpClass();

    buildMethods();

    return autoHttpClass;
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
      TypeMirror returnType = methodElement.getReturnType();
      MethodSpec.Builder methodBuilder =
          MethodSpec.methodBuilder(methodElement.getSimpleName().toString())
              .addAnnotation(Override.class)
              .addModifiers(
                  Sets.difference(methodElement.getModifiers(), ImmutableSet.of(ABSTRACT)))
              .returns(TypeName.get(returnType));

      //TODO method body impl

      autoHttpClass.addMethod(methodBuilder.build());
    }
  }
}
