package bio.overture.dms.core.properties;

import bio.overture.dms.core.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class FlywayProperties {

  @EnvVariable("SPRING_FLYWAY_ENABLED")
  private final boolean enabled;

  @NonNull
  @EnvVariable("SPRING_FLYWAY_LOCATIONS")
  private final String locations;
}
