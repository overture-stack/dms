package bio.overture.dms.rest.params;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Collection;

import static bio.overture.dms.core.util.Joiner.COMMA;
import static java.lang.String.format;

@Value
@Builder
public class QueryParam {
  @NonNull private final String key;
  @NonNull private final Object value;

  public static QueryParam createQueryParam(String key, Collection values) {
    return new QueryParam(key, COMMA.join(values));
  }

  @Override
  public String toString() {
    return format("%s=%s", key, value);
  }
}
