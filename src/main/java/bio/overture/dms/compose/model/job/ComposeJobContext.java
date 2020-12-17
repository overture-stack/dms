package bio.overture.dms.compose.model.job;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ComposeJobContext {

  @NonNull private final ComposeJob containerDeploy;
}
