package bio.overture.dms.properties.ego;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = PRIVATE)
public class RefreshTokenProperties {

  @NonNull private final Long durationMs;

  @NonNull private final Boolean cookieIsSecure;

  @NonNull private final String domain;
}
