package bio.overture.dms.compose.deployment.song;

import static bio.overture.dms.ego.model.PermissionMasks.WRITE;

import bio.overture.dms.compose.deployment.SimpleProvisionService;
import bio.overture.dms.core.model.dmsconfig.AppCredential;
import bio.overture.dms.ego.client.EgoService;
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
  @NonNull private final String scorePolicyName;
  @NonNull private final AppCredential appCredential;
  @NonNull private final EgoService egoService;

  @Override
  public void run() {
  }

}
