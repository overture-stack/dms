package bio.overture.dms.compose.deployment.song;

import static bio.overture.dms.compose.deployment.SimpleProvisionService.createSimpleProvisionService;
import static bio.overture.dms.compose.model.ComposeServiceResources.SONG_API;
import static bio.overture.dms.compose.model.Constants.DMS_ADMIN_GROUP_NAME;
import static bio.overture.dms.compose.model.Constants.SCORE_POLICY_NAME;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.compose.deployment.ego.EgoHelper;
import bio.overture.dms.core.Messenger;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.model.dmsconfig.SongConfig.SongApiConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SongApiDeployer {

  /** Constants */
  private static final String SONG_POLICY_NAME = "SONG";

  /** Dependencies */
  private final ServiceDeployer serviceDeployer;

  private final EgoHelper egoHelper;

  private final Messenger messenger;

  @Autowired
  public SongApiDeployer(@NonNull ServiceDeployer serviceDeployer,
                         @NonNull EgoHelper egoHelper,
                         @NonNull Messenger messenger) {
    this.serviceDeployer = serviceDeployer;
    this.egoHelper = egoHelper;
    this.messenger = messenger;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    egoHelper.waitForEgoApiHealthy(dmsConfig.getClusterRunMode(), dmsConfig.getEgo());
    serviceDeployer.deploy(dmsConfig, SONG_API, true);
    messenger.send("⏳ Provisioning needed data for '%s' ", SONG_API.toString());
    provision(dmsConfig);
    messenger.send("✔️ Provisioning for '%s' completed", SONG_API.toString());
    messenger.send("\uD83C\uDFC1️ Deployment for service %s finished successfully", SONG_API.toString());
  }

  private void provision(DmsConfig dmsConfig) {
    buildEgoSongProvisioner(dmsConfig.getEgo(), dmsConfig.getSong().getApi()).run();
  }

  private EgoSongProvisioner buildEgoSongProvisioner(
      EgoConfig egoConfig, SongApiConfig songApiConfig) {
    val egoService = egoHelper.buildEgoService(egoConfig);
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
}
