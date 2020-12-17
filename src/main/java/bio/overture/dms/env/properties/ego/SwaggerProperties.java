package bio.overture.dms.env.properties.ego;

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
public class SwaggerProperties {

  @NonNull private final String host;

  @NonNull private final String baseUrl;
}
