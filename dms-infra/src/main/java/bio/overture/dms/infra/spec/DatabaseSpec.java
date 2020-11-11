package bio.overture.dms.infra.spec;

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
public class DatabaseSpec {

  @NonNull
  private final String url;

  @NonNull
  private final String username;

  @NonNull
  private final String password;
}
