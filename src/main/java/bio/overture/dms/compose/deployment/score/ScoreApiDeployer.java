package bio.overture.dms.compose.deployment.score;

import static bio.overture.dms.compose.deployment.SimpleProvisionService.createSimpleProvisionService;
import static bio.overture.dms.compose.model.ComposeServiceResources.SCORE_API;
import static bio.overture.dms.compose.model.Constants.DMS_ADMIN_GROUP_NAME;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.compose.deployment.ego.EgoHelper;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig.ScoreApiConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScoreApiDeployer {

  /** Constants */
  private static final String SCORE_POLICY_NAME = "SCORE";

  /** Dependencies */
  private final ServiceDeployer serviceDeployer;

  private final EgoHelper egoHelper;

  @Autowired
  public ScoreApiDeployer(@NonNull ServiceDeployer serviceDeployer, @NonNull EgoHelper egoHelper) {
    this.serviceDeployer = serviceDeployer;
    this.egoHelper = egoHelper;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    egoHelper.waitForEgoApiHealthy(dmsConfig.getClusterRunMode(), dmsConfig.getEgo());
    serviceDeployer.deploy(dmsConfig, SCORE_API, true);
    provision(dmsConfig);
  }

  private void provision(DmsConfig dmsConfig) {
    buildEgoScoreProvisioner(dmsConfig.getEgo(), dmsConfig.getScore().getApi()).run();
  }

  private EgoScoreProvisioner buildEgoScoreProvisioner(
      EgoConfig egoConfig, ScoreApiConfig scoreApiConfig) {
    val egoService = egoHelper.buildEgoService(egoConfig);
    val simpleProvisionService = createSimpleProvisionService(egoService);
    return EgoScoreProvisioner.builder()
        .simpleProvisionService(simpleProvisionService)
        .dmsGroupName(DMS_ADMIN_GROUP_NAME)
        .scorePolicyName(SCORE_POLICY_NAME)
        .scoreAppCredential(scoreApiConfig.getScoreAppCredential())
        .build();
  }
}
