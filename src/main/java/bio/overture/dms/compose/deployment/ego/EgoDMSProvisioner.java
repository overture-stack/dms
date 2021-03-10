package bio.overture.dms.compose.deployment.ego;

import bio.overture.dms.compose.deployment.SimpleProvisionService;
import bio.overture.dms.core.model.dmsconfig.AppCredential;
import bio.overture.dms.ego.client.EgoService;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static bio.overture.dms.ego.model.PermissionMasks.WRITE;

/** Provisions necessary DMS groups, application and permissions in EGO in an idempotent manner */
@Builder
@RequiredArgsConstructor
public class EgoDMSProvisioner implements Runnable {

  @NonNull private final SimpleProvisionService simpleProvisionService;
  @NonNull private final String dmsGroupName;
  @NonNull private final String dmsPolicyName;
  @NonNull private final AppCredential egoUiAppCredential;
  @NonNull private final String songPolicyName;
  @NonNull private final String scorePolicyName;
  @NonNull private final AppCredential songAppCredential;
  @NonNull private final EgoService egoService;
  @NonNull private final AppCredential scoreAppCredential;

  @Override
  public void run() {
    // ego ui application
    simpleProvisionService.provisionGroupWritePermission(dmsGroupName, dmsPolicyName);
    simpleProvisionService.provisionApplication(egoUiAppCredential);

    // score application and policy
    simpleProvisionService.provisionGroupWritePermission(dmsGroupName, scorePolicyName);
    simpleProvisionService.provisionApplication(scoreAppCredential);

    // song application and policy
    simpleProvisionService.provisionGroupWritePermission(dmsGroupName, songPolicyName);
    simpleProvisionService.provisionApplication(songAppCredential);
    egoService.createApplicationPermission(songAppCredential.getName(), scorePolicyName, WRITE);
  }
}
