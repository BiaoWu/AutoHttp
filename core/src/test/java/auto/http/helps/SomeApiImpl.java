package auto.http.helps;

import java.lang.reflect.Type;

import auto.http.internal.AutoHttpPlugins;
import auto.http.internal.Call;
import auto.http.internal.ResultFactory;
import auto.http.internal.Utils;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author biaowu
 */
public class SomeApiImpl implements SomeApi {
  private AutoHttpPlugins autoHttpPlugins;

  public SomeApiImpl(AutoHttpPlugins autoHttpPlugins) {
    this.autoHttpPlugins = autoHttpPlugins;
  }

  @Override public Call<String> getString() {
    Request request = new Request.Builder()
        .url(autoHttpPlugins.baseUrl())
        .build();

    Type returnType = Utils.wrapType(Call.class, String.class);
    Type convertType = String.class;
    return ResultFactory.result(request, autoHttpPlugins, returnType, convertType);
  }

  @Override public Call<String> postString(String body) {
    RequestBody requestBody = RequestBody.create(MediaType.parse(""), body);
    Request request = new Request.Builder()
        .url(autoHttpPlugins.baseUrl())
        .method("POST", requestBody)
        .build();

    Type returnType = Utils.wrapType(Call.class, String.class);
    Type convertType = String.class;
    return ResultFactory.result(request, autoHttpPlugins, returnType, convertType);
  }
}
