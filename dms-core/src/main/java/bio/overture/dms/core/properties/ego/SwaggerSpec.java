package bio.overture.dms.core.properties.ego;

import bio.overture.dms.core.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SwaggerSpec {

  @NonNull
  private final String host;

  @NonNull
  private final String baseUrl;

}
