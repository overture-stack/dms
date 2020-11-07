package bio.overture.dms.core.properties;

import bio.overture.dms.core.properties.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DBServiceProperties implements ServiceProperties {

  @NonNull
  @EnvVariable("POSTGRES_DB")
  private final String dbName;

  @NonNull
  @EnvVariable("POSTGRES_USER")
  private final String username;

  @EnvVariable("POSTGRES_PASS")
  private final String password;

}
