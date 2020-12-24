/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package bio.overture.dms.rest.okhttp;

import static bio.overture.dms.rest.okhttp.OkHttpException.checkResponse;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;

import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.rest.RestClient;
import java.util.function.Function;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

@Value
@Builder
public class OkHttpRestClient implements RestClient {

  /** Constants */
  private static final String CONTENT_TYPE = "Content-Type";

  private static final String APPLICATION_JSON = "application/json";
  private static final String AUTHORIZATION = "Authorization";
  private static final String BASIC = "Basic";
  private static final String BEARER = "Bearer";

  /** Dependencies */
  @NonNull private final OkHttpClient okHttpClient;

  @NonNull private final ObjectSerializer jsonSerializer;

  @Override
  public String getString(String endpoint) {
    return tryRequest(RequestType.GET, endpoint, null);
  }

  @Override
  public String postString(String endpoint, Object body) {
    return tryRequest(RequestType.POST, endpoint, body);
  }

  @Override
  public String putString(String endpoint, Object body) {
    return tryRequest(RequestType.PUT, endpoint, body);
  }

  private String tryRequest(
      @NonNull RequestType requestType, @NonNull String endpoint, Object body) {
    val requestBody = createBody(body);
    var rb = newNoAuthRequestBuilder().url(endpoint);

    switch (requestType) {
      case GET:
        rb = rb.get();
        break;
      case POST:
        rb = rb.post(requestBody);
        break;
      case PUT:
        rb = rb.put(requestBody);
        break;
    }

    val r = rb.build();
    return doRequest(r);
  }

  private String doRequest(Request r) {
    return doRequest(r, identity());
  }

  @SneakyThrows
  private <R> R doRequest(Request r, Function<String, R> transformer) {
    try (val response = okHttpClient.newCall(r).execute()) {
      checkResponse(response);
      if (nonNull(response.body()) && nonNull(response.body().toString())) {
        return transformer.apply(response.body().string());
      }
      return null;
    }
  }

  private RequestBody createBody(Object body) {
    return RequestBody.create(
        isNull(body) ? "{}" : jsonSerializer.serializeValue(body), MediaType.get(APPLICATION_JSON));
  }

  private static Request.Builder newNoAuthRequestBuilder() {
    return new Request.Builder().header(CONTENT_TYPE, APPLICATION_JSON);
  }

  private enum RequestType {
    GET,
    POST,
    PUT;
  }
}
