package bio.overture.dms.ego;

import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.ego.client.EgoClient;
import bio.overture.dms.ego.client.EgoEndpoint;
import bio.overture.dms.ego.exception.EgoClientInstantiationException;
import bio.overture.dms.ego.model.EgoToken;
import bio.overture.dms.rest.RestClientFactory;
import bio.overture.dms.rest.RestClientHttpException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static bio.overture.dms.core.util.Strings.isDefined;
import static java.lang.String.format;

/** This factory is responsible for building an EgoClient using a RestClientFactory */
@Slf4j
@Getter
@Component
public class EgoClientFactory {

  private final ObjectSerializer jsonSerializer;
  private final RestClientFactory restClientFactory;

  @Builder
  @Autowired
  public EgoClientFactory(
      @NonNull ObjectSerializer jsonSerializer,
      @NonNull @Qualifier("retryingRestClientFactory") RestClientFactory restClientFactory) {
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

  public EgoClient buildAuthDmsEgoClient(@NonNull EgoConfig egoConfig){
    try {
      val serverUrl = egoConfig.getServerUrl().toString();
      val egoToken = buildNoAuthEgoClient(serverUrl)
          .postAccessToken(
              egoConfig.getDmsAppCredentials().getClientId(),
              egoConfig.getDmsAppCredentials().getClientSecret());
      return buildBearerAuthEgoClient(serverUrl, egoToken);
    } catch (RestClientHttpException e) {
      String message = null;
      if (e.isUnauthorizedError()) {
        message = format("The credentials for the Ego application '%s' are not valid",
            egoConfig.getDmsAppCredentials().getName());
      } else if (e.isForbiddenError()) {
        message = format("The credentials for the Ego application '%s' are forbidden",
            egoConfig.getDmsAppCredentials().getName());
      } else if (e.isError()) {
        message = format("An unexpected error occurred while creating an access token from the application '%s'",
            egoConfig.getDmsAppCredentials().getName());
      }
      log.error((isDefined(message) ? message + ": " : "UnknownEgoClient Error: " ) + e.getMessage());
      throw new EgoClientInstantiationException(message, e);
    }
  }

}
