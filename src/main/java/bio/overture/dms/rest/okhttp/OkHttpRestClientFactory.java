package bio.overture.dms.rest.okhttp;

import static bio.overture.dms.core.util.Joiner.WHITESPACE;
import static bio.overture.dms.core.util.Strings.isDefined;

import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.rest.RestClient;
import bio.overture.dms.rest.RestClientFactory;
import bio.overture.dms.rest.RetryingRestClientDecorator;
import java.io.IOException;
import java.time.Duration;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jodah.failsafe.RetryPolicy;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This factory is responsible for building a RestClient that is implemented using OkHttp as the
 * http client, and FailSafe as the retry client.
 */
@Slf4j
@Builder
@RequiredArgsConstructor
public class OkHttpRestClientFactory implements RestClientFactory {

  /** Constants */
  private static final String CONTENT_TYPE = "Content-Type";

  private static final String APPLICATION_JSON = "application/json";
  private static final String AUTHORIZATION = "Authorization";
  private static final String BEARER = "Bearer";

  /** Dependencies */
  @NonNull private final ObjectSerializer jsonSerializer;

  /** Configuration */
  @NonNull private final Duration callTimeout;

  @NonNull private final Duration connectTimeout;
  @NonNull private final Duration readTimeout;
  @NonNull private final Duration writeTimeout;
  @NonNull private final RetryPolicy<String> retryPolicy;

  @Override
  public RestClient buildBearerAuthRestClient(@NonNull String bearerToken) {
    return buildRestClient(bearerToken);
  }

  @Override
  public RestClient buildNoAuthRestClient() {
    return buildRestClient(null);
  }

  private RestClient buildRestClient(String bearerToken) {
    val internalRestClient =
        OkHttpRestClient.builder()
            .okHttpClient(buildOkHttpClient(bearerToken))
            .jsonSerializer(jsonSerializer)
            .build();
    return new RetryingRestClientDecorator(internalRestClient, retryPolicy);
  }

  private OkHttpClient buildOkHttpClient(String bearerToken) {
    return new OkHttpClient.Builder()
        .addInterceptor(c -> interceptRequest(c, bearerToken))
        .retryOnConnectionFailure(true)
        .readTimeout(readTimeout)
        .callTimeout(callTimeout)
        .connectTimeout(connectTimeout)
        .writeTimeout(writeTimeout)
        .build();
  }

  private static Response interceptRequest(Chain chain, String bearerToken) throws IOException {
    val r = chain.request();

    val builder = new Request.Builder(r).header(CONTENT_TYPE, APPLICATION_JSON);
    if (isDefined(bearerToken)) {
      builder.header(AUTHORIZATION, buildBearerAuthValue(bearerToken));
    }
    return chain.proceed(builder.build());
  }

  private static String buildBearerAuthValue(String token) {
    return WHITESPACE.join(BEARER, token);
  }
}
