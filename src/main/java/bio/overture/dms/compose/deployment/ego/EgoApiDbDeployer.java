package bio.overture.dms.compose.deployment.ego;

import static bio.overture.dms.compose.deployment.SimpleProvisionService.createSimpleProvisionService;
import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_API;
import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_DB;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EgoApiDbDeployer {

  /** Constants */
  private static final String DMS_ADMIN_GROUP_NAME = "dms-admin";

  private static final String DMS_POLICY_NAME = "DMS";

  /** Dependencies */
  private final ServiceDeployer serviceDeployer;

  private final EgoHelper egoHelper;

  @Autowired
  public EgoApiDbDeployer(@NonNull ServiceDeployer serviceDeployer, @NonNull EgoHelper egoHelper) {
    this.serviceDeployer = serviceDeployer;
    this.egoHelper = egoHelper;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    // TODO: DB deployment is not blocking the api deployement....fix this
    serviceDeployer.deploy(dmsConfig, EGO_DB, true);
    serviceDeployer.deploy(dmsConfig, EGO_API, true);
    egoHelper.waitForEgoApiHealthy(dmsConfig.getClusterRunMode(), dmsConfig.getEgo());
    provision(dmsConfig);
  }

  private void provision(DmsConfig dmsConfig) {
    buildEgoDmsProvisioner(dmsConfig.getEgo()).run();
  }

  private EgoDMSProvisioner buildEgoDmsProvisioner(EgoConfig egoConfig) {
    val egoService = egoHelper.buildEgoService(egoConfig);
    val simpleProvisionService = createSimpleProvisionService(egoService);
    return EgoDMSProvisioner.builder()
        .simpleProvisionService(simpleProvisionService)
        .dmsGroupName(DMS_ADMIN_GROUP_NAME)
        .dmsPolicyName(DMS_POLICY_NAME)
        .egoUiAppCredential(egoConfig.getUi().getUiAppCredential())
        .build();
  }
}
