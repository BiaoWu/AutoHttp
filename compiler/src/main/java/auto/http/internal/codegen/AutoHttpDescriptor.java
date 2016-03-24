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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
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
    abstract TypeMirror returnType();
  }

  static class Factory {
    static AutoHttpDescriptor create(
        TypeElement validTypeElement,
        ImmutableList<ExecutableElement> validMethodElements) {

      ImmutableList.Builder<MethodDescriptor> methodDescriptorBuilder = ImmutableList.builder();
      for (ExecutableElement methodElement : validMethodElements) {
        MethodDescriptor methodDescriptor =
            new AutoValue_AutoHttpDescriptor_MethodDescriptor(
                methodElement,
                methodElement.getReturnType()
            );
        methodDescriptorBuilder.add(methodDescriptor);
      }

      return new AutoValue_AutoHttpDescriptor(
          validTypeElement,
          methodDescriptorBuilder.build());
    }
  }
}
