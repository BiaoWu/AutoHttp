/*
 * Copyright (C) 2014 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package auto.http.internal.codegen;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Generated;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A template class that provides a framework for properly handling IO while generating source files
 * from an annotation processor.  Particularly, it makes a best effort to ensure that files that
 * fail to write successfully are deleted.
 *
 * @param <T> The input type from which source is to be generated.
 */
abstract class JavaPoetSourceFileGenerator<T> {
  private static final String GENERATED_COMMENTS = "https://biaowu.github.io/AutoHttp";

  private static final AnnotationSpec GENERATED =
      AnnotationSpec.builder(Generated.class)
          .addMember("value", "$S", AutoHttpProcessor.class.getName())
          .addMember("comments", "$S", GENERATED_COMMENTS)
          .build();

  private final Filer filer;
  private final boolean generatedAnnotationAvailable;

  JavaPoetSourceFileGenerator(Filer filer, Elements elements) {
    this.filer = checkNotNull(filer);
    generatedAnnotationAvailable = elements.getTypeElement("javax.annotation.Generated") != null;
  }

  /** Generates a source file to be compiled for {@code T}. */
  void generate(T input) throws SourceFileGenerationException {
    ClassName generatedTypeName = nameGeneratedType(input);
    try {
      Optional<TypeSpec.Builder> type = write(generatedTypeName, input);
      if (!type.isPresent()) {
        return;
      }
      JavaFile javaFile = buildJavaFile(generatedTypeName, type.get());

      final JavaFileObject sourceFile = filer.createSourceFile(
          generatedTypeName.toString(),
          Iterables.toArray(javaFile.typeSpec.originatingElements, Element.class));
      try {
        new Formatter().formatSource(
            CharSource.wrap(javaFile.toString()),
            new CharSink() {
              @Override public Writer openStream() throws IOException {
                return sourceFile.openWriter();
              }
            });
      } catch (FormatterException e) {
        throw new SourceFileGenerationException(
            Optional.of(generatedTypeName), e, getElementForErrorReporting(input));
      }
    } catch (Exception e) {
      // if the code above threw a SFGE, use that
      Throwables.propagateIfPossible(e, SourceFileGenerationException.class);
      // otherwise, throw a new one
      throw new SourceFileGenerationException(
          Optional.<ClassName>absent(), e, getElementForErrorReporting(input));
    }
  }

  private JavaFile buildJavaFile(
      ClassName generatedTypeName, TypeSpec.Builder typeSpecBuilder) {
    if (generatedAnnotationAvailable) {
      typeSpecBuilder.addAnnotation(GENERATED);
    }
    JavaFile.Builder javaFileBuilder =
        JavaFile.builder(generatedTypeName.packageName(), typeSpecBuilder.build())
            .skipJavaLangImports(true);
    if (!generatedAnnotationAvailable) {
      javaFileBuilder.addFileComment(
          "Generated by %s (%s).", AutoHttpProcessor.class.getName(), GENERATED_COMMENTS);
    }
    return javaFileBuilder.build();
  }

  /**
   * Implementations should return the {@link ClassName} for the top-level type to be generated.
   */
  abstract ClassName nameGeneratedType(T input);

  /**
   * Returns an optional element to be used for reporting errors. This returns a single element
   * rather than a collection to reduce output noise.
   */
  abstract Optional<? extends Element> getElementForErrorReporting(T input);

  /**
   * Returns a {@link TypeSpec.Builder type} to be generated for {@code T}, or {@link
   * Optional#absent()} if no file should be generated.
   */
  // TODO(ronshapiro): write() makes more sense in JavaWriter where all writers are mutable.
  // consider renaming to something like typeBuilder() which conveys the mutability of the result
  abstract Optional<TypeSpec.Builder> write(ClassName generatedTypeName, T input);
}
