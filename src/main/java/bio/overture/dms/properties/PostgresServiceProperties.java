package bio.overture.dms.properties;

import bio.overture.dms.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PostgresServiceProperties implements ServiceProperties {

  @NonNull
  @EnvVariable("POSTGRES_DB")
  private final String dbName;

  @NonNull
  @EnvVariable("POSTGRES_USER")
  private final String username;

  @NonNull
  @EnvVariable("POSTGRES_PASSWORD")
  private final String password;
}
