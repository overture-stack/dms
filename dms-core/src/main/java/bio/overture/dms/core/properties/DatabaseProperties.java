package bio.overture.dms.core.properties;

import bio.overture.dms.core.properties.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DatabaseProperties {

  @NonNull
  @EnvVariable("SPRING_DATASOURCE_URL")
  private final String url;

  @NonNull
  @EnvVariable("SPRING_DATASOURCE_USERNAME")
  private final String username;

  @NonNull
  @EnvVariable("SPRING_DATASOURCE_PASSWORD")
  private final String password;
}
