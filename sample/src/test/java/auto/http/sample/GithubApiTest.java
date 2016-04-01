package auto.http.sample;

import org.junit.Test;

import java.io.IOException;

import auto.http.internal.AutoHttpPlugins;
import auto.http.internal.Response;
import auto.http.sample.helps.ToStringConverterFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author biaowu
 */
public class GithubApiTest {
  private AutoHttpPlugins plugins = new AutoHttpPlugins.Builder()
      .baseUrl("https://api.github.com/")
      .build();
  private GithubApi githubApi = new AutoHttp_GithubApi(plugins.newBuilder()
      .addConverterFactory(new ToStringConverterFactory())
      .build());

  @Test public void testGetRoot() throws IOException {
    Response<String> response = githubApi.getRoot().execute();

    System.out.println(response.body());
    assertThat(response.isSuccessful()).isTrue();
  }
}
