package bio.overture.dms.compose.deployment.ego;

import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_API;
import static bio.overture.dms.core.model.enums.ClusterRunModes.LOCAL;
import static bio.overture.dms.core.model.enums.ClusterRunModes.PRODUCTION;
import static bio.overture.dms.core.util.Exceptions.buildIllegalStateException;
import static bio.overture.dms.ego.client.EgoService.createEgoService;

import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.ego.EgoClientFactory;
import bio.overture.dms.ego.client.EgoClient;
import bio.overture.dms.ego.client.EgoService;
import bio.overture.dms.rest.RestClientFactory;
import bio.overture.dms.swarm.properties.DockerProperties;
import java.net.URL;
import java.time.Duration;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EgoHelper {

  /** Constants */
  private static final RetryPolicy<String> RETRY_POLICY =
      new RetryPolicy<String>().withMaxRetries(10).withDelay(Duration.ofSeconds(10));

  /** Dependencies */
  private final EgoClientFactory egoClientFactory;

  private final RestClientFactory basicRestClientFactory;
  private final boolean runInDocker;

  @Autowired
  public EgoHelper(
      @NonNull EgoClientFactory egoClientFactory,
      @NonNull DockerProperties dockerProperties,
      @NonNull @Qualifier("nonRetryingRestClientFactory")
          RestClientFactory basicRestClientFactory) {
    this.egoClientFactory = egoClientFactory;
    this.basicRestClientFactory = basicRestClientFactory;
    this.runInDocker = dockerProperties.getRunAs();
  }

  /**
   * If the DMS installer is run AS A DOCKER CONTAINER and is connected to the dms network (i.e
   * runInDocker == true), then use the ego api service name to access the service.
   *
   * @param egoApiConfig
   * @return
   */
  @SneakyThrows
  public URL getLocalEgoApiUrl(EgoConfig.EgoApiConfig egoApiConfig) {
    if (runInDocker) {
      // 8080 is the internal port in the cluster network, not changeable
      return new URL("http://" + EGO_API.toString() + ":8080");
    } else {
      return new URL("http://localhost:" + egoApiConfig.getHostPort());
    }
  }

  // TODO: refactor this to use the buildEgoService method instead.
  public void waitForEgoApiHealthy(ClusterRunModes clusterRunMode, EgoConfig egoConfig) {
    // Build an ego client that does not have a built in retry mechanism
    val egoClientFactory2 =
        EgoClientFactory.builder()
            .jsonSerializer(egoClientFactory.getJsonSerializer())
            .restClientFactory(basicRestClientFactory)
            .build();

    EgoClient egoClient = null;
    if (clusterRunMode == LOCAL) {
      egoClient =
          egoClientFactory2.buildNoAuthEgoClient(getLocalEgoApiUrl(egoConfig.getApi()).toString());
    } else if (clusterRunMode == PRODUCTION) {
      egoClient = egoClientFactory2.buildNoAuthEgoClient(egoConfig.getApi().getUrl().toString());
    } else {
      throw buildIllegalStateException(
          "The clusterRunMode '%s' could not be processed", clusterRunMode);
    }

    // Attempt to get the public key several times
    Failsafe.with(RETRY_POLICY).get(egoClient::getPublicKey);
  }

  public EgoService buildEgoService(EgoConfig egoConfig) {
    val egoClient = egoClientFactory.buildAuthDmsEgoClient(egoConfig.getApi().getDmsAppCredential(),
        getLocalEgoApiUrl(egoConfig.getApi()).toString());
    return createEgoService(egoClient);
  }
}
