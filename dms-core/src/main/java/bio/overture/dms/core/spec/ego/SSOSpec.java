package bio.overture.dms.core.spec.ego;

import bio.overture.dms.core.env.EnvVariable;
import lombok.AccessLevel;
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
public class SSOSpec {

  @NonNull
  @EnvVariable("CLIENT_ID")
  private final String clientId;

  @NonNull
  @EnvVariable("CLIENT_SECRET")
  private final String clientSecret;

  @NonNull
  private final String preEstablishedRedirectUri;

}
