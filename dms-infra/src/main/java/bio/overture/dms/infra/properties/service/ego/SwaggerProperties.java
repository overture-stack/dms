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
public class SwaggerProperties {

  @NonNull
  private final String host;

  @NonNull
  private final String baseUrl;

}
