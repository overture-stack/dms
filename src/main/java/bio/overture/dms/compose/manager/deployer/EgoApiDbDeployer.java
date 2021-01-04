package bio.overture.dms.compose.manager.deployer;

import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_API;
import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_DB;

import bio.overture.dms.compose.manager.ServiceDeployer;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig2;
import bio.overture.dms.ego.EgoClientFactory;
import bio.overture.dms.rest.RestClientFactory;
import java.time.Duration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EgoApiDbDeployer {

  // 10 minute retry policy
  private static final RetryPolicy<String> RETRY_POLICY =
      new RetryPolicy<String>().withMaxRetries(300).withDelay(Duration.ofSeconds(2));

  private final ServiceDeployer serviceDeployer;
  private final EgoClientFactory egoClientFactory;
  private final RestClientFactory basicRestClientFactory;

  @Autowired
  public EgoApiDbDeployer(
      @NonNull ServiceDeployer serviceDeployer,
      @NonNull EgoClientFactory egoClientFactory,
      @NonNull @Qualifier("nonRetryingRestClientFactory")
          RestClientFactory basicRestClientFactory) {
    this.serviceDeployer = serviceDeployer;
    this.egoClientFactory = egoClientFactory;
    this.basicRestClientFactory = basicRestClientFactory;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    // TODO: DB deployment is not blocking the api deployement....fix this
    val dbDeployType = serviceDeployer.deployAndWait(dmsConfig, EGO_DB);
    val apiDeployType = serviceDeployer.deployAndWait(dmsConfig, EGO_API);

    waitForEgoApiHealthy(dmsConfig.getEgo());
    attemptFinalization(dmsConfig.getEgo());
  }

  private void waitForEgoApiHealthy(EgoConfig2 egoConfig) {
    // Build an ego client that does not have a built in retry mechanism
    val egoClient =
        EgoClientFactory.builder()
            .jsonSerializer(egoClientFactory.getJsonSerializer())
            .restClientFactory(basicRestClientFactory)
            .build()
            .buildNoAuthEgoClient(egoConfig.getApi().getUrl().toString());

    // Attempt to get the public key several times
    Failsafe.with(RETRY_POLICY).get(egoClient::getPublicKey);
  }

  private void attemptFinalization(EgoConfig2 egoConfig) {
    val dmsEgoClient = egoClientFactory.buildAuthDmsEgoClient(egoConfig);
    // TODO: implement properly
    log.info("DOOOOOOOOOOOOOOOOOOOO STUFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
  }
}
