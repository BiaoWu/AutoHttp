/*
 * Copyright (C) 2015 Square, Inc.
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
package auto.http.helps;

import java.io.IOException;
import java.lang.reflect.Type;

import auto.http.internal.Converter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class ToStringConverterFactory extends Converter.Factory {
  static final MediaType MEDIA_TYPE = MediaType.parse("text/plain");

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type) {
    if (String.class.equals(type)) {
      return new Converter<ResponseBody, String>() {
        @Override public String convert(ResponseBody value) throws IOException {
          return value.string();
        }
      };
    }
    return null;
  }

  @Override public Converter<?, RequestBody> requestBodyConverter(Type type) {
    if (String.class.equals(type)) {
      return new Converter<String, RequestBody>() {
        @Override public RequestBody convert(String value) throws IOException {
          return RequestBody.create(MEDIA_TYPE, value);
        }
      };
    }
    return null;
  }
}