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

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import auto.http.AutoHttp;

/**
 * @author biaowu
 */
final class AutoHttpProcessingStep implements BasicAnnotationProcessor.ProcessingStep {
  private final Messager messager;
  private final AutoHttpGenerator autoHttpGenerator;
  private final AutoHttpValidator autoHttpValidator;
  private final MethodValidator methodValidator;

  AutoHttpProcessingStep(
      Messager messager,
      AutoHttpGenerator autoHttpGenerator,
      AutoHttpValidator autoHttpValidator,
      MethodValidator methodValidator) {
    this.messager = messager;
    this.autoHttpGenerator = autoHttpGenerator;
    this.autoHttpValidator = autoHttpValidator;
    this.methodValidator = methodValidator;
  }

  @Override public Set<? extends Class<? extends Annotation>> annotations() {
    return ImmutableSet.of(
        AutoHttp.class
    );
  }

  @Override
  public Set<Element> process(
      SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
    ImmutableSet.Builder<TypeElement> validAutoHttpTypesBuilder = ImmutableSet.builder();
    for (Element autoHttpElement : elementsByAnnotation.get(AutoHttp.class)) {
      TypeElement autoHttpTypeElement = MoreElements.asType(autoHttpElement);

      ValidationReport<TypeElement> report = autoHttpValidator.validate(autoHttpTypeElement);
      report.printMessagesTo(messager);
      if (report.isClean()) {
        validAutoHttpTypesBuilder.add(autoHttpTypeElement);
      }
    }

    for (TypeElement validAutoHttpTypeElement : validAutoHttpTypesBuilder.build()) {
      ImmutableList.Builder<ExecutableElement> validMethodsBuilder = ImmutableList.builder();
      for (Element declaredElement : validAutoHttpTypeElement.getEnclosedElements()) {
        if (declaredElement.getKind() == ElementKind.METHOD) {
          ExecutableElement methodElement = MoreElements.asExecutable(declaredElement);

          ValidationReport<ExecutableElement> report = methodValidator.validate(methodElement);
          report.printMessagesTo(messager);
          if (report.isClean()) {
            validMethodsBuilder.add(methodElement);
          }
        }
      }
      AutoHttpDescriptor autoHttpDescriptor =
          AutoHttpDescriptor.Factory.create(
              validAutoHttpTypeElement,
              validMethodsBuilder.build());

      try {
        autoHttpGenerator.generate(autoHttpDescriptor);
      } catch (SourceFileGenerationException e) {
        e.printMessageTo(messager);
      }
    }
    return ImmutableSet.of();
  }
}
