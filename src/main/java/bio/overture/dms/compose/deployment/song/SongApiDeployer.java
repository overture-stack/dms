package bio.overture.dms.compose.deployment.song;

import static bio.overture.dms.compose.deployment.SimpleProvisionService.createSimpleProvisionService;
import static bio.overture.dms.compose.model.ComposeServiceResources.SONG_API;
import static bio.overture.dms.compose.model.Constants.DMS_ADMIN_GROUP_NAME;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.compose.deployment.ego.EgoHelper;
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

  @Autowired
  public SongApiDeployer(@NonNull ServiceDeployer serviceDeployer, @NonNull EgoHelper egoHelper) {
    this.serviceDeployer = serviceDeployer;
    this.egoHelper = egoHelper;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    egoHelper.waitForEgoApiHealthy(dmsConfig.getClusterRunMode(), dmsConfig.getEgo());
    serviceDeployer.deploy(dmsConfig, SONG_API, true);
    provision(dmsConfig);
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
        .songPolicyName(SONG_POLICY_NAME)
        .songAppCredential(songApiConfig.getSongAppCredential())
        .scoreAppCredential(songApiConfig.getScoreAppCredential())
        .build();
  }
}
