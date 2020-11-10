package bio.overture.dms.core.spec;

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
