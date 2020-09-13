package bio.overture.dms.core.properties.service.song;

import bio.overture.dms.core.properties.service.ServiceProperties;
import bio.overture.dms.core.properties.service.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SongDBServiceProperties implements ServiceProperties {
  @NonNull private final String name;

  @EnvVariable("POSTGRES_DB")
  @NonNull
  private final String dbName;

  @EnvVariable("POSTGRES_USER")
  @NonNull
  private final String username;

  @EnvVariable("POSTGRES_PASS")
  @NonNull
  private final String password;
}
