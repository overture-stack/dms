package bio.overture.dms.compose.model;

import bio.overture.dms.core.model.dmsconfig.AppCredential;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class EgoDmsProvisionSpec {

  @NonNull private final String dmsGroupName;
  @NonNull private final String dmsPolicyName;
//  @NonNull private final String songPolicyName;
//  @NonNull private final String scorePolicyName;
  @NonNull private final AppCredential egoUiAppCredential;
//  @NonNull private final AppCredential dmsUiAppCredential;
}
