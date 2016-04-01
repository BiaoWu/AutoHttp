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

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

/**
 * @author biaowu
 */
final class AutoHttpGenerator extends JavaPoetSourceFileGenerator<AutoHttpDescriptor> {
  private static final String PREFIX = "AutoHttp_";

  private final Messager messager;

  AutoHttpGenerator(Filer filer, Elements elements, Messager messager) {
    super(filer, elements);
    this.messager = messager;
  }

  @Override ClassName nameGeneratedType(AutoHttpDescriptor input) {
    ClassName className = ClassName.get(input.autoHttpElement());
    String componentName =
        PREFIX + Joiner.on('_').join(className.simpleNames());
    return className.topLevelClassName().peerClass(componentName);
  }

  @Override Optional<? extends Element> getElementForErrorReporting(AutoHttpDescriptor input) {
    return Optional.of(input.autoHttpElement());
  }

  @Override
  Optional<TypeSpec.Builder> write(ClassName generatedTypeName, AutoHttpDescriptor input) {
    return Optional.of(
        new AutoHttpWriter(input, generatedTypeName, messager).write());
  }
}
