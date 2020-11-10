package bio.overture.dms.core.properties.ego;

import bio.overture.dms.core.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SSOProperties {

  @NonNull
  @EnvVariable("CLIENT_ID")
  private final String clientId;

  @NonNull
  @EnvVariable("CLIENT_SECRET")
  private final String clientSecret;

  @NonNull
  @EnvVariable("PREESTABLISHEDREDIRECTURI")
  private final String preEstablishedRedirectUri;

}
