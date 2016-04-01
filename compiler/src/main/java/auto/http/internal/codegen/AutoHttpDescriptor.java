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

import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;


/**
 * @author biaowu
 */
@AutoValue
abstract class AutoHttpDescriptor {
  abstract TypeElement autoHttpElement();

  abstract ImmutableList<MethodDescriptor> methodElements();

  @AutoValue
  static abstract class MethodDescriptor {
    abstract ExecutableElement methodElement();

    abstract TypeName returnTypeName();

    abstract Class<?> returnType();

    abstract ImmutableList<Class<?>> returnTypeArguments();

    abstract Class<?> converterType();

    abstract ImmutableList<Class<?>> converterTypeArguments();
  }

  static class Factory {
    static AutoHttpDescriptor create(
        TypeElement validTypeElement,
        ImmutableList<ExecutableElement> validMethodElements) throws ClassNotFoundException {

      ImmutableList.Builder<MethodDescriptor> methodDescriptorBuilder = ImmutableList.builder();
      for (ExecutableElement methodElement : validMethodElements) {
        TypeMirror returnTypeMirror = methodElement.getReturnType();

        DeclaredType declaredType = MoreTypes.asDeclared(returnTypeMirror);
        Class<?> returnType =
            Class.forName(declaredType.asElement().toString());

        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        Class<?>[] returnTypeArguments = new Class<?>[typeArguments.size()];
        for (int i = 0; i < returnTypeArguments.length; i++) {
          returnTypeArguments[i] =
              Class.forName(MoreTypes.asDeclared(typeArguments.get(i)).toString());
        }

        Class<?> converterType = null;
        Class<?>[] converterTypeArguments = null;
        if (returnTypeArguments.length > 0) {
          converterType = returnTypeArguments[0];
        }

        if (returnTypeArguments.length > 1) {
          converterTypeArguments = Arrays.copyOfRange(returnTypeArguments, 1, returnTypeArguments.length);
        } else {
          converterTypeArguments = new Class[0];
        }


        MethodDescriptor methodDescriptor =
            new AutoValue_AutoHttpDescriptor_MethodDescriptor(
                methodElement,
                TypeName.get(returnTypeMirror),
                returnType,
                ImmutableList.copyOf(returnTypeArguments),
                converterType,
                ImmutableList.copyOf(converterTypeArguments)
            );
        methodDescriptorBuilder.add(methodDescriptor);
      }

      return new AutoValue_AutoHttpDescriptor(
          validTypeElement,
          methodDescriptorBuilder.build());
    }
  }
}
