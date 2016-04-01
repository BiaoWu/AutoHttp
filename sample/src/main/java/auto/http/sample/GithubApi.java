package auto.http.sample;

import auto.http.AutoHttp;
import auto.http.internal.Call;

/**
 * @author biaowu
 */
@AutoHttp
public interface GithubApi {
  Call<String> getRoot();
}
