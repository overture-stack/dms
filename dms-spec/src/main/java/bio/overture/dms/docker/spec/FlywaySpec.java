package bio.overture.dms.docker.spec;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = PRIVATE)
public class FlywaySpec {

  private final boolean enabled;

  private final String locations;
}
