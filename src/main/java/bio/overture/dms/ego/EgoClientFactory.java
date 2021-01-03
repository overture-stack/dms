package bio.overture.dms.ego;

import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.ego.client.EgoClient;
import bio.overture.dms.ego.client.EgoEndpoint;
import bio.overture.dms.ego.model.EgoToken;
import bio.overture.dms.rest.RestClientFactory;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** This factory is responsible for building an EgoClient using a RestClientFactory */
@Component
public class EgoClientFactory {

  private final ObjectSerializer jsonSerializer;
  private final RestClientFactory restClientFactory;

  @Autowired
  public EgoClientFactory(
      @NonNull ObjectSerializer jsonSerializer, @NonNull RestClientFactory restClientFactory) {
    this.jsonSerializer = jsonSerializer;
    this.restClientFactory = restClientFactory;
  }

  public EgoClient buildNoAuthEgoClient(@NonNull String baseServerUrl) {
    return new EgoClient(
        new EgoEndpoint(baseServerUrl), jsonSerializer, restClientFactory.buildNoAuthRestClient());
  }

  public EgoClient buildBearerAuthEgoClient(
      @NonNull String baseServerUrl, @NonNull EgoToken egoToken) {
    return buildBearerAuthEgoClient(baseServerUrl, egoToken.getAccessToken());
  }

  public EgoClient buildBearerAuthEgoClient(@NonNull String baseServerUrl, @NonNull String token) {
    return new EgoClient(
        new EgoEndpoint(baseServerUrl),
        jsonSerializer,
        restClientFactory.buildBearerAuthRestClient(token));
  }

  public EgoClient buildDmsEgoClient(@NonNull EgoConfig egoConfig){
    val serverUrl = egoConfig.getServerUrl().toString();
    val egoToken = buildNoAuthEgoClient(serverUrl)
        .postAccessToken(
            egoConfig.getDmsAppCredentials().getClientId(),
            egoConfig.getDmsAppCredentials().getClientSecret());
    return buildBearerAuthEgoClient(serverUrl, egoToken);
  }

}
