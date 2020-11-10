package bio.overture.dms.core.spec.ego;

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
