package bio.overture.dms.compose.manager.deployer;

import bio.overture.dms.compose.manager.ServiceDeployer;
import bio.overture.dms.compose.manager.ServiceDeployer.DeployTypes;
import bio.overture.dms.compose.manager.ServiceSpecRenderEngine;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.ego.EgoClientFactory;
import bio.overture.dms.rest.RestClientFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_API;
import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_DB;

@Slf4j
@Component
public class EgoApiDbDeployer {

  // 10 minute retry policy
  private static final RetryPolicy<String> RETRY_POLICY = new RetryPolicy<String>()
      .withMaxRetries(300)
      .withDelay(Duration.ofSeconds(2));

  private final ServiceSpecRenderEngine serviceSpecRenderEngine;
  private final ServiceDeployer serviceDeployer;
  private final EgoClientFactory egoClientFactory;
  private final RestClientFactory basicRestClientFactory;

  @Autowired
  public EgoApiDbDeployer(
      @NonNull ServiceSpecRenderEngine serviceSpecRenderEngine,
      @NonNull ServiceDeployer serviceDeployer,
      @NonNull EgoClientFactory egoClientFactory,
      @NonNull @Qualifier("nonRetryingRestClientFactory") RestClientFactory basicRestClientFactory) {
    this.serviceSpecRenderEngine = serviceSpecRenderEngine;
    this.serviceDeployer = serviceDeployer;
    this.egoClientFactory = egoClientFactory;
    this.basicRestClientFactory = basicRestClientFactory;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    val dbDeployType = deployDb(dmsConfig);
    val apiDeployType = deployApi(dmsConfig);

    waitForEgoApiHealthy(dmsConfig.getEgo());
    attemptFinalization(dmsConfig.getEgo());
  }

  //TODO: DB deployment is not blocking the api deployement....fix this
  private DeployTypes deployDb(DmsConfig dmsConfig) {
    val dbServiceSpec = serviceSpecRenderEngine.render(dmsConfig, EGO_DB)
        .orElseThrow();
    val dbDeployType = serviceDeployer.process(dbServiceSpec);
    serviceDeployer.waitForServiceRunning(dbServiceSpec);
    return dbDeployType;
  }

  private DeployTypes deployApi(DmsConfig dmsConfig) {
    val apiServiceSpec = serviceSpecRenderEngine.render(dmsConfig, EGO_API)
        .orElseThrow();
    val apiDeployType = serviceDeployer.process(apiServiceSpec);
    serviceDeployer.waitForServiceRunning(apiServiceSpec);
    return apiDeployType;
  }

  private void waitForEgoApiHealthy(EgoConfig egoConfig) {
    // Build an ego client that does not have a built in retry mechanism
    val egoClient = EgoClientFactory.builder()
        .jsonSerializer(egoClientFactory.getJsonSerializer())
        .restClientFactory(basicRestClientFactory)
        .build()
        .buildNoAuthEgoClient(egoConfig.getServerUrl().toString());

    // Attempt to get the public key several times
    Failsafe.with(RETRY_POLICY)
        .get(egoClient::getPublicKey);
  }

  private void attemptFinalization(EgoConfig egoConfig) {
    val dmsEgoClient = egoClientFactory.buildAuthDmsEgoClient(egoConfig);
    // TODO: implement properly
    log.info("DOOOOOOOOOOOOOOOOOOOO STUFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

  }

}
