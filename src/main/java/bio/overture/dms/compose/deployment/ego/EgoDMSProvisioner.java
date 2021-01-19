package bio.overture.dms.compose.deployment.ego;

import bio.overture.dms.compose.deployment.SimpleProvisionService;
import bio.overture.dms.core.model.dmsconfig.AppCredential;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Provisions necessary DMS groups, application and permissions in EGO in an idempotent manner */
@Builder
@RequiredArgsConstructor
public class EgoDMSProvisioner implements Runnable {

  @NonNull private final SimpleProvisionService simpleProvisionService;
  @NonNull private final String dmsGroupName;
  @NonNull private final String dmsPolicyName;
  @NonNull private final AppCredential egoUiAppCredential;

  @Override
  public void run() {
    simpleProvisionService.provisionGroupWritePermission(dmsGroupName, dmsPolicyName);
    simpleProvisionService.provisionApplication(egoUiAppCredential);
  }
}
