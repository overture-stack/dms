package bio.overture.dms.core.properties.ego;

import bio.overture.dms.core.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
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
