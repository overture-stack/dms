package bio.overture.dms.core.properties;

import bio.overture.dms.core.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DatabaseSpec {

  @NonNull
  private final String url;

  @NonNull
  private final String username;

  @NonNull
  private final String password;
}
