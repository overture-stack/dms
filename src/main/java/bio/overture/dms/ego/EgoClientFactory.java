package bio.overture.dms.ego;

import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.ego.client.EgoClient;
import bio.overture.dms.ego.client.EgoEndpoint;
import bio.overture.dms.ego.model.EgoToken;
import bio.overture.dms.rest.okhttp.OkHttpRestClientFactory;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EgoClientFactory {

  private final ObjectSerializer jsonSerializer;
  private final OkHttpRestClientFactory okHttpRestClientFactory;

  @Autowired
  public EgoClientFactory(
      @NonNull ObjectSerializer jsonSerializer,
      @NonNull OkHttpRestClientFactory okHttpRestClientFactory) {
    this.jsonSerializer = jsonSerializer;
    this.okHttpRestClientFactory = okHttpRestClientFactory;
  }

  public EgoClient buildNoAuthEgoClient(@NonNull String baseServerUrl) {
    return new EgoClient(
        new EgoEndpoint(baseServerUrl),
        jsonSerializer,
        okHttpRestClientFactory.buildNoAuthRestClient());
  }

  public EgoClient buildBearerAuthEgoClient(
      @NonNull String baseServerUrl, @NonNull EgoToken egoToken) {
    return buildBearerAuthEgoClient(baseServerUrl, egoToken.getAccessToken());
  }

  public EgoClient buildBearerAuthEgoClient(@NonNull String baseServerUrl, @NonNull String token) {
    return new EgoClient(
        new EgoEndpoint(baseServerUrl),
        jsonSerializer,
        okHttpRestClientFactory.buildBearerAuthRestClient(token));
  }
}
