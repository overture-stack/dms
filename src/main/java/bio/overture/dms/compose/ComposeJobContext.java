package bio.overture.dms.compose;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ComposeJobContext {

  @NonNull private final ComposeJob imagePull;
  @NonNull private final ComposeJob containerDeploy;
}
