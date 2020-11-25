package bio.overture.dms.infra.env.visitor;

import static java.util.Objects.isNull;

import bio.overture.dms.core.util.Nullable;
import java.util.Map;
import lombok.NonNull;

/** Implementation of a env field visitor that persists a valid environment variable */
public class ProcessEnvFieldVisitor implements EnvFieldVisitor {

  @Override
  public void visit(
      @NonNull Map<String, String> envMap, @NonNull String envName, @Nullable Object fieldValue) {
    if (isValid(fieldValue)) {
      envMap.put(envName, fieldValue.toString());
    }
  }

  private boolean isValid(@Nullable Object o) {
    return !isNull(o);
  }
}
