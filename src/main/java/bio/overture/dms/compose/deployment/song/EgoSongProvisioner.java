package bio.overture.dms.compose.deployment.song;

import bio.overture.dms.compose.deployment.SimpleProvisionService;
import bio.overture.dms.core.model.dmsconfig.AppCredential;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Provisions necessary DMS groups, application and permissions in EGO in an idempotent manner */
@Builder
@RequiredArgsConstructor
public class EgoSongProvisioner implements Runnable {

  @NonNull private final SimpleProvisionService simpleProvisionService;
  @NonNull private final String dmsGroupName;
  @NonNull private final String songPolicyName;
  @NonNull private final AppCredential songAppCredential;
  @NonNull private final AppCredential scoreAppCredential;

  @Override
  public void run() {
    simpleProvisionService.provisionGroupWritePermission(dmsGroupName, songPolicyName);
    simpleProvisionService.provisionApplication(songAppCredential);
    simpleProvisionService.provisionApplication(scoreAppCredential);
  }
}
