package bio.overture.dms.core.properties.ego;

import bio.overture.dms.core.properties.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SwaggerProperties {

  @NonNull
  @EnvVariable("HOST")
  private final String host;

  @NonNull
  @EnvVariable("BASEURL")
  private final String baseUrl;

}
