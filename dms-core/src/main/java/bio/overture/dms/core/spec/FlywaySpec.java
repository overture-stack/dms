package bio.overture.dms.core.spec;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class FlywaySpec {

  private final boolean enabled;

  @NonNull
  private final String locations;
}
