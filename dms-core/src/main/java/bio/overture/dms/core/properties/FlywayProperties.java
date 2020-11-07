package bio.overture.dms.core.properties;

import bio.overture.dms.core.properties.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class FlywayProperties {

  @EnvVariable("SPRING_FLYWAY_ENABLED")
  private final boolean enabled;

  @EnvVariable("SPRING_FLYWAY_LOCATIONS")
  @NonNull
  private final String locations;
}
