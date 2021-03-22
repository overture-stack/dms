package bio.overture.dms.compose.deployment.song;

import static bio.overture.dms.compose.model.ComposeServiceResources.SONG_API;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.compose.deployment.ego.EgoHelper;
import bio.overture.dms.core.Messenger;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SongApiDeployer {

  /** Dependencies */
  private final ServiceDeployer serviceDeployer;

  private final EgoHelper egoHelper;

  private final Messenger messenger;

  @Autowired
  public SongApiDeployer(
      @NonNull ServiceDeployer serviceDeployer,
      @NonNull EgoHelper egoHelper,
      @NonNull Messenger messenger) {
    this.serviceDeployer = serviceDeployer;
    this.egoHelper = egoHelper;
    this.messenger = messenger;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    egoHelper.waitForEgoApiHealthy(dmsConfig.getClusterRunMode(), dmsConfig.getEgo());
    serviceDeployer.deploy(dmsConfig, SONG_API, true);
    messenger.send(
        "\uD83C\uDFC1️ Deployment for service %s finished successfully", SONG_API.toString());
  }
}
