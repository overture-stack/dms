package bio.overture.dms.core.properties;

import bio.overture.dms.core.env.EnvVariable;
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
