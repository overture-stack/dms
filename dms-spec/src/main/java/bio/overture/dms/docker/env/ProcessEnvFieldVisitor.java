package bio.overture.dms.docker.env;

import bio.overture.dms.core.Nullable;
import lombok.NonNull;

import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Implementation of a env field visitor that persists a valid environment variable
 */
public class ProcessEnvFieldVisitor implements EnvFieldVisitor {

  @Override
  public void visit(@NonNull Map<String, String> envMap, @NonNull String envName, @Nullable Object fieldValue) {
    if (isValid(fieldValue)) {
      envMap.put(envName, fieldValue.toString());
    }
  }

  private boolean isValid(@Nullable Object o){
    return !isNull(o);
  }

}
