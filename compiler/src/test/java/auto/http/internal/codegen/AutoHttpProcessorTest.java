package auto.http.internal.codegen;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * @author biaowu
 */
public class AutoHttpProcessorTest {

  @Test public void autoHttpOnClass() {
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.XXApi",
        "package test;",
        "",
        "import auto.http.AutoHttp;",
        "",
        "@AutoHttp",
        "class XXApi {}");
    assertAbout(javaSource()).that(componentFile)
        .processedWith(new AutoHttpProcessor())
        .failsToCompile()
        .withErrorContaining("interface");
  }

  @Test public void autoHttpOnInterface() {
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.XXApi",
        "package test;",
        "",
        "import auto.http.AutoHttp;",
        "",
        "@AutoHttp",
        "interface XXApi {}");
    assertAbout(javaSource()).that(componentFile)
        .processedWith(new AutoHttpProcessor())
        .compilesWithoutError();
  }

  @Test public void voidMethod() {
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.XXApi2",
        "package test;",
        "",
        "import auto.http.AutoHttp;",
        "",
        "@AutoHttp",
        "interface XXApi2 {",
        "  void testMethod();",
        "}");
    assertAbout(javaSource()).that(componentFile)
        .processedWith(new AutoHttpProcessor())
        .failsToCompile()
        .withErrorContaining("void");
  }

}