package bio.overture.dms.core.spec.ego;

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
public class RefreshTokenSpec {

  @NonNull
  private final int durationMs;

  @NonNull
  private final boolean cookieIsSecure;

  @NonNull
  private final String domain;

}
