package bio.overture.dms.compose.deployment.ego;

import static bio.overture.dms.compose.deployment.ego.EgoDMSProvisioner.createEgoDMSProvisioner;
import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_API;
import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_DB;
import static bio.overture.dms.ego.client.EgoService.createEgoService;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.compose.model.EgoDmsProvisionSpec;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
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

  private static final String DMS_ADMIN_GROUP_NAME = "dms-admin";
  private static final String APPROVED = "APPROVED";
  private static final String DMS_POLICY_NAME = "DMS";

  // 10 minute retry policy
  private static final RetryPolicy<String> RETRY_POLICY =
      new RetryPolicy<String>().withMaxRetries(300).withDelay(Duration.ofSeconds(2));
  private static final String SONG_POLICY_NAME = "SONG";
  private static final String SCORE_POLICY_NAME = "SCORE";

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
    serviceDeployer.deploy(dmsConfig, EGO_DB, true);
    serviceDeployer.deploy(dmsConfig, EGO_API, true);
    waitForEgoApiHealthy(dmsConfig.getEgo());
    provision(dmsConfig);
  }

  private void waitForEgoApiHealthy(EgoConfig egoConfig) {
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

  private void provision(DmsConfig dmsConfig) {
    val provisioner = buildEgoDmsProvisioner(dmsConfig.getEgo());
    val spec = buildDefaultProvisionSpec(dmsConfig);
    provisioner.provision(spec);
  }

  private EgoDMSProvisioner buildEgoDmsProvisioner(EgoConfig egoConfig) {
    val egoClient = egoClientFactory.buildAuthDmsEgoClient(egoConfig);
    val egoService = createEgoService(egoClient);
    return createEgoDMSProvisioner(egoService);
  }

  private EgoDmsProvisionSpec buildDefaultProvisionSpec(DmsConfig dmsConfig) {
    return EgoDmsProvisionSpec.builder()
        .dmsGroupName(DMS_ADMIN_GROUP_NAME)
        .dmsPolicyName(DMS_POLICY_NAME)
        .egoUiAppCredential(dmsConfig.getEgo().getUi().getUiAppCredential())
        //        .songPolicyName(SONG_POLICY_NAME)
        //        .scorePolicyName(SCORE_POLICY_NAME)
        //        .dmsUiAppCredential()
        .build();
  }
}
