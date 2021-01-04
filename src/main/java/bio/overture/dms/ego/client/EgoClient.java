package bio.overture.dms.ego.client;

import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.ego.model.EgoToken;
import bio.overture.dms.rest.RestClient;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.NotImplementedException;

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

  public EgoApplication createApplication(@NonNull CreateApplicationRequest r) {
    return restClient.post(
        egoEndpoint.postCreateApplication(),
        r,
        x -> jsonSerializer.convertValue(x, EgoApplication.class));
  }

  public Optional<EgoApplication> findApplicationByName(@NonNull String applicationName) {
    // TODO: ego client find app by name, needs to page through everything
    throw new NotImplementedException();
    //    restClient.post(egoEndpoint.postCreateApplication(), null,
    //        x -> jsonSerializer.deserialize(x).path("id").textValue());
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateApplicationRequest {
    @NotNull private String name;
    @NotNull private String type;
    @NotNull private String clientId;
    @NotNull private String clientSecret;
    private String redirectUri;
    private String description;
    @NotNull private String status;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class EgoApplication {
    @NotNull private String id;
    @NotNull private String name;
    @NotNull private String type;
    @NotNull private String clientId;
    @NotNull private String clientSecret;
    private String redirectUri;
    private String description;
    @NotNull private String status;
  }
}
