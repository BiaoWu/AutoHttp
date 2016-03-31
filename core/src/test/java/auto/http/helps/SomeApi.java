package auto.http.helps;

import auto.http.internal.Call;

/**
 * @author biaowu
 */
public interface SomeApi {
  Call<String> getString();

  Call<String> postString(String body);
}
