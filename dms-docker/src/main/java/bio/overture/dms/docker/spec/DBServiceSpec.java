package bio.overture.dms.docker.spec;

import bio.overture.dms.docker.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DBServiceSpec implements ServiceSpec {

  @NonNull
  @EnvVariable("POSTGRES_DB")
  private final String dbName;

  @NonNull
  @EnvVariable("POSTGRES_USER")
  private final String username;

  @EnvVariable("POSTGRES_PASS")
  private final String password;

}
