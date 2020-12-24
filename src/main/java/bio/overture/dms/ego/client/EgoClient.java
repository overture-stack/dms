package bio.overture.dms.ego.client;

import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.ego.model.EgoToken;
import bio.overture.dms.rest.RestClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/** A api client for an externally running Ego service */
@RequiredArgsConstructor
public class EgoClient {

  /** Dependencies */
  @NonNull private final EgoEndpoint egoEndpoint;

  @NonNull private final ObjectSerializer jsonSerializer;
  @NonNull private final RestClient restClient;

  @SneakyThrows
  public EgoToken postAccessToken(@NonNull String clientId, @NonNull String clientSecret) {
    return restClient.post(
        egoEndpoint.postAccessToken(clientId, clientSecret),
        s -> jsonSerializer.convertValue(s, EgoToken.class));
  }

  @SneakyThrows
  public String getPublicKey() {
    return restClient.getString(egoEndpoint.getPublicKey());
  }
}
