/*
 * Copyright (C) 2012 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package auto.http.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static auto.http.internal.Utils.checkNotNull;
import static java.util.Collections.unmodifiableList;

/**
 * Copy from Retrofit
 * But some different
 * <p>
 * For example,
 * <pre>{@code
 * }</pre>
 *
 * @author Bob Lee (bob@squareup.com)
 * @author Jake Wharton (jw@squareup.com)
 * @author biaowu
 */
public final class AutoHttpPlugins {
  private final okhttp3.Call.Factory callFactory;
  private final HttpUrl baseUrl;
  private final List<Converter.Factory> converterFactories;
  private final List<CallAdapter.Factory> adapterFactories;
  private final Executor callbackExecutor;

  AutoHttpPlugins(okhttp3.Call.Factory callFactory,
                  HttpUrl baseUrl,
                  List<Converter.Factory> converterFactories,
                  List<CallAdapter.Factory> adapterFactories,
                  Executor callbackExecutor) {
    this.callFactory = callFactory;
    this.baseUrl = baseUrl;
    this.converterFactories = unmodifiableList(converterFactories); // Defensive copy at call site.
    this.adapterFactories = unmodifiableList(adapterFactories); // Defensive copy at call site.
    this.callbackExecutor = callbackExecutor;
  }

  /**
   * The factory used to create {@linkplain okhttp3.Call OkHttp calls} for sending a HTTP requests.
   * Typically an instance of {@link OkHttpClient}.
   */
  public okhttp3.Call.Factory callFactory() {
    return callFactory;
  }

  /** The API base URL. */
  public HttpUrl baseUrl() {
    return baseUrl;
  }

  /**
   * Returns a list of the factories tried when creating a
   * {@linkplain #callAdapter(Type)} call adapter}.
   */
  public List<CallAdapter.Factory> callAdapterFactories() {
    return adapterFactories;
  }

  /**
   * Returns the {@link CallAdapter} for {@code returnType} from the available {@linkplain
   * #callAdapterFactories() factories}.
   *
   * @throws IllegalArgumentException if no call adapter available for {@code type}.
   */
  public CallAdapter<?> callAdapter(Type returnType) {
    return nextCallAdapter(null, returnType);
  }

  /**
   * Returns the {@link CallAdapter} for {@code returnType} from the available {@linkplain
   * #callAdapterFactories() factories} except {@code skipPast}.
   *
   * @throws IllegalArgumentException if no call adapter available for {@code type}.
   */
  public CallAdapter<?> nextCallAdapter(CallAdapter.Factory skipPast, Type returnType) {
    checkNotNull(returnType, "returnType == null");

    int start = adapterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = adapterFactories.size(); i < count; i++) {
      CallAdapter<?> adapter = adapterFactories.get(i).get(returnType);
      if (adapter != null) {
        return adapter;
      }
    }

    StringBuilder builder = new StringBuilder("Could not locate call adapter for ")
        .append(returnType)
        .append(".\n");
    if (skipPast != null) {
      builder.append("  Skipped:");
      for (int i = 0; i < start; i++) {
        builder.append("\n   * ").append(adapterFactories.get(i).getClass().getName());
      }
      builder.append('\n');
    }
    builder.append("  Tried:");
    for (int i = start, count = adapterFactories.size(); i < count; i++) {
      builder.append("\n   * ").append(adapterFactories.get(i).getClass().getName());
    }
    throw new IllegalArgumentException(builder.toString());
  }

  /**
   * Returns a {@link Converter} for {@code type} to {@link RequestBody} from the available
   * {@linkplain #converterFactories factories}.
   *
   * @throws IllegalArgumentException if no converter available for {@code type}.
   */
  public <T> Converter<T, RequestBody> requestBodyConverter(Type type) {
    return nextRequestBodyConverter(null, type);
  }

  /**
   * Returns a {@link Converter} for {@code type} to {@link RequestBody} from the available
   * {@linkplain #converterFactories factories} except {@code skipPast}.
   *
   * @throws IllegalArgumentException if no converter available for {@code type}.
   */
  public <T> Converter<T, RequestBody> nextRequestBodyConverter(Converter.Factory skipPast, Type type) {
    checkNotNull(type, "type == null");

    int start = converterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = converterFactories.size(); i < count; i++) {
      Converter.Factory factory = converterFactories.get(i);
      Converter<?, RequestBody> converter = factory.requestBodyConverter(type);
      if (converter != null) {
        //noinspection unchecked
        return (Converter<T, RequestBody>) converter;
      }
    }

    StringBuilder builder = new StringBuilder("Could not locate RequestBody converter for ")
        .append(type)
        .append(".\n");
    if (skipPast != null) {
      builder.append("  Skipped:");
      for (int i = 0; i < start; i++) {
        builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
      }
      builder.append('\n');
    }
    builder.append("  Tried:");
    for (int i = start, count = converterFactories.size(); i < count; i++) {
      builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
    }
    throw new IllegalArgumentException(builder.toString());
  }

  /**
   * Returns a {@link Converter} for {@link ResponseBody} to {@code type} from the available
   * {@linkplain #converterFactories factories}.
   *
   * @throws IllegalArgumentException if no converter available for {@code type}.
   */
  public <T> Converter<ResponseBody, T> responseBodyConverter(Type type) {
    return nextResponseBodyConverter(null, type);
  }

  /**
   * Returns a {@link Converter} for {@link ResponseBody} to {@code type} from the available
   * {@linkplain #converterFactories factories} except {@code skipPast}.
   *
   * @throws IllegalArgumentException if no converter available for {@code type}.
   */
  public <T> Converter<ResponseBody, T> nextResponseBodyConverter(Converter.Factory skipPast, Type type) {
    checkNotNull(type, "type == null");

    int start = converterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = converterFactories.size(); i < count; i++) {
      Converter<ResponseBody, ?> converter =
          converterFactories.get(i).responseBodyConverter(type);
      if (converter != null) {
        //noinspection unchecked
        return (Converter<ResponseBody, T>) converter;
      }
    }

    StringBuilder builder = new StringBuilder("Could not locate ResponseBody converter for ")
        .append(type)
        .append(".\n");
    if (skipPast != null) {
      builder.append("  Skipped:");
      for (int i = 0; i < start; i++) {
        builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
      }
      builder.append('\n');
    }
    builder.append("  Tried:");
    for (int i = start, count = converterFactories.size(); i < count; i++) {
      builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
    }
    throw new IllegalArgumentException(builder.toString());
  }

  /**
   * Returns a {@link Converter} for {@code type} to {@link String} from the available
   * {@linkplain #converterFactories factories}.
   */
  public <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
    checkNotNull(type, "type == null");
    checkNotNull(annotations, "annotations == null");

    for (int i = 0, count = converterFactories.size(); i < count; i++) {
      Converter<?, String> converter =
          converterFactories.get(i).stringConverter(type);
      if (converter != null) {
        //noinspection unchecked
        return (Converter<T, String>) converter;
      }
    }

    // Nothing matched. Resort to default converter which just calls toString().
    //noinspection unchecked
    return (Converter<T, String>) BuiltInConverters.ToStringConverter.INSTANCE;
  }

  /**
   * The executor used for {@link Callback} methods on a {@link Call}. This may be {@code null},
   * in which case callbacks should be made synchronously on the background thread.
   */
  public Executor callbackExecutor() {
    return callbackExecutor;
  }

  public Builder newBuilder() {
    if (this.baseUrl == null)
      throw new IllegalArgumentException("this Plugins must init before!");

    Builder builder = new Builder();
    builder.baseUrl(this.baseUrl);
    for (CallAdapter.Factory factory : this.adapterFactories) {
      builder.addCallAdapterFactory(factory);
    }
    for (Converter.Factory factory : this.converterFactories) {
      builder.addConverterFactory(factory);
    }
    if (this.callbackExecutor != null) {
      builder.callbackExecutor(this.callbackExecutor);
    }
    if (this.callFactory != null) {
      builder.callFactory(this.callFactory);
    }
    return builder;
  }

  /**
   * Build a new {@link AutoHttpPlugins}.
   * <p>
   * Calling {@link #baseUrl} is required before calling {@link #build()}. All other methods
   * are optional.
   */
  public static final class Builder {
    private okhttp3.Call.Factory callFactory;
    private HttpUrl baseUrl;
    private List<Converter.Factory> converterFactories = new ArrayList<>();
    private List<CallAdapter.Factory> adapterFactories = new ArrayList<>();
    private Executor callbackExecutor;

    public Builder() {
      // Add the built-in converter factory first. This prevents overriding its behavior but also
      // ensures correct behavior when using converters that consume all types.
      converterFactories.add(new BuiltInConverters());
    }

    /**
     * The HTTP client used for requests.
     * <p>
     * This is a convenience method for calling {@link #callFactory}.
     * <p>
     * Note: This method <b>does not</b> make a defensive copy of {@code client}. Changes to its
     * settings will affect subsequent requests. Pass in a {@linkplain OkHttpClient#clone() cloned}
     * instance to prevent this if desired.
     */
    public Builder client(OkHttpClient client) {
      return callFactory(checkNotNull(client, "client == null"));
    }

    /**
     * Specify a custom call factory for creating {@link Call} instances.
     * <p>
     * Note: Calling {@link #client} automatically sets this value.
     */
    public Builder callFactory(okhttp3.Call.Factory factory) {
      this.callFactory = checkNotNull(factory, "factory == null");
      return this;
    }

    /**
     * Set the API base URL.
     *
     * @see #baseUrl(HttpUrl)
     */
    public Builder baseUrl(String baseUrl) {
      checkNotNull(baseUrl, "baseUrl == null");
      HttpUrl httpUrl = HttpUrl.parse(baseUrl);
      if (httpUrl == null) {
        throw new IllegalArgumentException("Illegal URL: " + baseUrl);
      }
      return baseUrl(httpUrl);
    }

    /**
     * Set the API base URL.
     * <p>
     * The specified endpoint values (such as with {@link GET @GET}) are resolved against this
     * value using {@link HttpUrl#resolve(String)}. The behavior of this matches that of an
     * {@code <a href="">} link on a website resolving on the current URL.
     * <p>
     * <b>Base URLs should always end in {@code /}.</b>
     * <p>
     * A trailing {@code /} ensures that endpoints values which are relative paths will correctly
     * append themselves to a base which has path components.
     * <p>
     * <b>Correct:</b><br>
     * Base URL: http://example.com/api/<br>
     * Endpoint: foo/bar/<br>
     * Result: http://example.com/api/foo/bar/
     * <p>
     * <b>Incorrect:</b><br>
     * Base URL: http://example.com/api<br>
     * Endpoint: foo/bar/<br>
     * Result: http://example.com/foo/bar/
     * <p>
     * This method enforces that {@code baseUrl} has a trailing {@code /}.
     * <p>
     * <b>Endpoint values which contain a leading {@code /} are absolute.</b>
     * <p>
     * Absolute values retain only the host from {@code baseUrl} and ignore any specified path
     * components.
     * <p>
     * Base URL: http://example.com/api/<br>
     * Endpoint: /foo/bar/<br>
     * Result: http://example.com/foo/bar/
     * <p>
     * Base URL: http://example.com/<br>
     * Endpoint: /foo/bar/<br>
     * Result: http://example.com/foo/bar/
     * <p>
     * <b>Endpoint values may be a full URL.</b>
     * <p>
     * Values which have a host replace the host of {@code baseUrl} and values also with a scheme
     * replace the scheme of {@code baseUrl}.
     * <p>
     * Base URL: http://example.com/<br>
     * Endpoint: https://github.com/square/retrofit/<br>
     * Result: https://github.com/square/retrofit/
     * <p>
     * Base URL: http://example.com<br>
     * Endpoint: //github.com/square/retrofit/<br>
     * Result: http://github.com/square/retrofit/ (note the scheme stays 'http')
     */
    public Builder baseUrl(HttpUrl baseUrl) {
      checkNotNull(baseUrl, "baseUrl == null");
      List<String> pathSegments = baseUrl.pathSegments();
      if (!"".equals(pathSegments.get(pathSegments.size() - 1))) {
        throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
      }
      this.baseUrl = baseUrl;
      return this;
    }

    /** Add converter factory for serialization and deserialization of objects. */
    public Builder addConverterFactory(Converter.Factory factory) {
      converterFactories.add(checkNotNull(factory, "factory == null"));
      return this;
    }

    /**
     * Add a call adapter factory for supporting service method return types other than {@link
     * Call}.
     */
    public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
      adapterFactories.add(checkNotNull(factory, "factory == null"));
      return this;
    }

    /**
     * The executor on which {@link Callback} methods are invoked when returning {@link Call} from
     * your service method.
     * <p>
     * Note: {@code executor} is not used for {@linkplain #addCallAdapterFactory custom method
     * return types}.
     */
    public Builder callbackExecutor(Executor executor) {
      this.callbackExecutor = checkNotNull(executor, "executor == null");
      return this;
    }

    /**
     * Create the {@link AutoHttpPlugins} instance using the configured values.
     * <p>
     * Note: If neither {@link #client} nor {@link #callFactory} is called a default {@link
     * OkHttpClient} will be created and used.
     */
    public AutoHttpPlugins build() {
      if (baseUrl == null) {
        throw new IllegalStateException("Base URL required.");
      }

      okhttp3.Call.Factory callFactory = this.callFactory;
      if (callFactory == null) {
        callFactory = new OkHttpClient();
      }

      // Make a defensive copy of the adapters and add the default Call adapter.
      List<CallAdapter.Factory> adapterFactories = new ArrayList<>(this.adapterFactories);
      if (callbackExecutor != null) {
        adapterFactories.add(new ExecutorCallAdapterFactory(callbackExecutor));
      } else {
        adapterFactories.add(DefaultCallAdapterFactory.INSTANCE);
      }

      // Make a defensive copy of the converters.
      List<Converter.Factory> converterFactories = new ArrayList<>(this.converterFactories);

      return new AutoHttpPlugins(
          callFactory,
          baseUrl,
          converterFactories,
          adapterFactories,
          callbackExecutor
      );
    }
  }
}
