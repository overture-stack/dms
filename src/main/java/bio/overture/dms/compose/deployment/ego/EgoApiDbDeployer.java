package bio.overture.dms.compose.deployment.ego;

import static bio.overture.dms.compose.deployment.SimpleProvisionService.createSimpleProvisionService;
import static bio.overture.dms.compose.deployment.song.SongApiDeployer.SONG_POLICY_NAME;
import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_API;
import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_DB;
import static bio.overture.dms.compose.model.Constants.SCORE_POLICY_NAME;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.compose.deployment.score.EgoScoreProvisioner;
import bio.overture.dms.compose.deployment.song.EgoSongProvisioner;
import bio.overture.dms.core.Messenger;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig;
import bio.overture.dms.core.model.dmsconfig.SongConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.ego.client.EgoService;
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

  private final Messenger messenger;

  @Autowired
  public EgoApiDbDeployer(
      @NonNull ServiceDeployer serviceDeployer,
      @NonNull EgoHelper egoHelper,
      @NonNull Messenger messenger) {
    this.serviceDeployer = serviceDeployer;
    this.egoHelper = egoHelper;
    this.messenger = messenger;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    // TODO: DB deployment is not blocking the api deployement....fix this
    serviceDeployer.deploy(dmsConfig, EGO_DB, true);
    messenger.send("\uD83C\uDFC1️ Deployment for '%s' finished ", EGO_DB.toString());

    serviceDeployer.deploy(dmsConfig, EGO_API, true);

    messenger.send("⏳ Waiting for '%s' service to be healthy..", EGO_API.toString());
    egoHelper.waitForEgoApiHealthy(dmsConfig.getClusterRunMode(), dmsConfig.getEgo());
    //    messenger.send("✔️ Service '%s' is healthy. ", EGO_API.toString());

    //    messenger.send("⏳ Provisioning needed data for '%s' ", EGO_API.toString());
    provision(dmsConfig);
    //    messenger.send("✔️ Provisioning for '%s' completed", EGO_API.toString());
    messenger.send("\uD83C\uDFC1️ Deployment for '%s' finished ", EGO_API.toString());
  }

  private void provision(DmsConfig dmsConfig) {
    val egoService = egoHelper.buildEgoService(dmsConfig.getEgo(), dmsConfig.getClusterRunMode());
    buildEgoDmsProvisioner(dmsConfig.getEgo(), egoService).run();
    buildEgoScoreProvisioner( dmsConfig.getScore().getApi(), egoService).run();
    buildEgoSongProvisioner(dmsConfig.getSong().getApi(), egoService).run();
  }

  private EgoDMSProvisioner buildEgoDmsProvisioner(EgoConfig egoConfig, EgoService egoService) {
    val simpleProvisionService = createSimpleProvisionService(egoService);
    return EgoDMSProvisioner.builder()
        .simpleProvisionService(simpleProvisionService)
        .dmsGroupName(DMS_ADMIN_GROUP_NAME)
        .dmsPolicyName(DMS_POLICY_NAME)
        .egoUiAppCredential(egoConfig.getUi().getUiAppCredential())
        .build();
  }

  private EgoSongProvisioner buildEgoSongProvisioner(
      SongConfig.SongApiConfig songApiConfig,
      EgoService egoService) {
    val simpleProvisionService = createSimpleProvisionService(egoService);
    return EgoSongProvisioner.builder()
        .simpleProvisionService(simpleProvisionService)
        .dmsGroupName(DMS_ADMIN_GROUP_NAME)
        .egoService(egoService)
        .songPolicyName(SONG_POLICY_NAME)
        .scorePolicyName(SCORE_POLICY_NAME)
        .appCredential(songApiConfig.getAppCredential())
        .build();
  }

  private EgoScoreProvisioner buildEgoScoreProvisioner(
      ScoreConfig.ScoreApiConfig scoreApiConfig,
      EgoService egoService) {
    val simpleProvisionService = createSimpleProvisionService(egoService);
    return EgoScoreProvisioner.builder()
        .simpleProvisionService(simpleProvisionService)
        .dmsGroupName(DMS_ADMIN_GROUP_NAME)
        .scorePolicyName(SCORE_POLICY_NAME)
        .scoreAppCredential(scoreApiConfig.getAppCredential())
        .build();
  }
}
