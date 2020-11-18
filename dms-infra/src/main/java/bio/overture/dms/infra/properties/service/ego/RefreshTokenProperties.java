package bio.overture.dms.infra.properties.service.ego;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = PRIVATE)
public class RefreshTokenProperties {

  @NonNull
  private final Long durationMs;

  @NonNull
  private final Boolean cookieIsSecure;

  @NonNull
  private final String domain;

}