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
public class ClientDatabaseProperties {

  @NonNull private final String url;

  @NonNull private final String username;

  @NonNull private final String password;
}
