package bio.overture.dms.env.properties.ego;

import static lombok.AccessLevel.PRIVATE;

import bio.overture.dms.env.EnvVariable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = PRIVATE)
public class SSOProperties {

  @NonNull
  @EnvVariable("CLIENT_ID")
  private final String clientId;

  @NonNull
  @EnvVariable("CLIENT_SECRET")
  private final String clientSecret;

  @NonNull private final String preEstablishedRedirectUri;
}
