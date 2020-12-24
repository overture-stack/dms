package bio.overture.dms.ego.client;

import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/** Resolves the endpoints for a specific api function */
@RequiredArgsConstructor
public class EgoEndpoint {

  @NonNull private final String baseServerUrl;

  public String postAccessToken(@NonNull String clientId, @NonNull String clientSecret) {

    return format(
        "%s/oauth/token?client_id=%s&client_secret=%s&grant_type=client_credentials",
        baseServerUrl, encodeValue(clientId), encodeValue(clientSecret));
  }

  public String getPublicKey() {
    return format("%s/oauth/token/public_key", baseServerUrl);
  }

  @SneakyThrows
  private static String encodeValue(String value) {
    return encode(value, UTF_8.toString());
  }
}
