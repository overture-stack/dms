package bio.overture.dms.core.properties;

import bio.overture.dms.core.properties.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DatabaseProperties {

  @EnvVariable("SPRING_DATASOURCE_URL")
  @NonNull
  private final String url;

  @EnvVariable("SPRING_DATASOURCE_USERNAME")
  @NonNull
  private final String username;

  @EnvVariable("SPRING_DATASOURCE_PASSWORD")
  @NonNull
  private final String password;
}
