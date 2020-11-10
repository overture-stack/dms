package bio.overture.dms.core.properties.ego;

import bio.overture.dms.core.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class RefreshTokenSpec {

  @NonNull
  private final int durationMs;

  @NonNull
  private final boolean cookieIsSecure;

  @NonNull
  private final String domain;

}
