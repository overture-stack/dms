package bio.overture.dms.compose.deployment.ego;

import static bio.overture.dms.compose.deployment.DmsComposeManager.resolveServiceHost;
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
      new RetryPolicy<String>().withMaxRetries(3).withDelay(Duration.ofSeconds(10));

  /** Dependencies */
  private final EgoClientFactory egoClientFactory;

  private final RestClientFactory basicRestClientFactory;

  @Autowired
  public EgoHelper(
      @NonNull EgoClientFactory egoClientFactory,
      @NonNull DockerProperties dockerProperties,
      @NonNull @Qualifier("nonRetryingRestClientFactory")
          RestClientFactory basicRestClientFactory) {
    this.egoClientFactory = egoClientFactory;
    this.basicRestClientFactory = basicRestClientFactory;
  }

  // TODO: refactor this to use the buildEgoService method instead.

  public void waitForEgoApiHealthy(ClusterRunModes clusterRunMode, EgoConfig egoConfig) {
    // Build an ego client that does not have a built in retry mechanism
    val egoClientFactory2 =
        EgoClientFactory.builder()
            .jsonSerializer(egoClientFactory.getJsonSerializer())
            .restClientFactory(basicRestClientFactory)
            .build();

    val url = "http://" + resolveServiceHost(clusterRunMode, egoConfig.getApi().getHostPort());
    val egoClient = egoClientFactory2.buildNoAuthEgoClient(url);

    // Attempt to get the public key several times
    Failsafe.with(RETRY_POLICY).get(egoClient::getPublicKey);
  }

  public EgoService buildEgoService(EgoConfig egoConfig, ClusterRunModes runModes) {
    val egoClient =
        egoClientFactory.buildAuthDmsEgoClient(
            egoConfig.getApi().getDmsAppCredential(),
            "http://" + resolveServiceHost(runModes, egoConfig.getApi().getHostPort())
        );
    return createEgoService(egoClient);
  }
}
