package bio.overture.dms.core.properties.ego;

import bio.overture.dms.core.properties.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class RefreshTokenProperties {

  @NonNull
  @EnvVariable("REFRESHTOKEN_DURATIONMS")
  private final int durationMs;

  @NonNull
  @EnvVariable("REFRESHTOKEN_COOKIEISSECURE")
  private final boolean cookieIsSecure;

  @NonNull
  @EnvVariable("REFRESHTOKEN_DOMAIN")
  private final String domain;

}
