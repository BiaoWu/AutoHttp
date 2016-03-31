package auto.http;

import org.junit.Test;

import java.io.IOException;

import auto.http.helps.SomeApi;
import auto.http.helps.SomeApiImpl;
import auto.http.helps.ToStringConverterFactory;
import auto.http.internal.AutoHttpPlugins;
import auto.http.internal.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author biaowu
 */
public class CallTest {
  private MockWebServer server = new MockWebServer();
  private AutoHttpPlugins base = new AutoHttpPlugins.Builder()
      .baseUrl(server.url("/"))
      .build();
  private SomeApi someApi = new SomeApiImpl(base.newBuilder()
      .addConverterFactory(new ToStringConverterFactory())
      .build());

  @Test public void testGetString() throws IOException {
    String body = "Hi Bill!";
    server.enqueue(new MockResponse().setBody(body));

    Response<String> response = someApi.getString().execute();

    assertThat(response.isSuccessful()).isTrue();
    assertThat(response.body()).isEqualTo(body);
  }

  @Test public void testPostString() throws IOException {
    String body = "I am Bill!";
    server.enqueue(new MockResponse());

    Response<String> response = someApi.postString(body).execute();

    assertThat(response.isSuccessful()).isTrue();
  }

}
