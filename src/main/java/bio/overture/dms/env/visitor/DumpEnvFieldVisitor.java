package bio.overture.dms.env.visitor;

import bio.overture.dms.core.util.Nullable;
import java.util.Map;
import lombok.NonNull;

/**
 * Implementation of a env field visitor that persists any environment variable to the map for
 * dumping purposes
 */
public class DumpEnvFieldVisitor implements EnvFieldVisitor {

  @Override
  public void visit(
      @NonNull Map<String, String> envMap, @NonNull String envName, @Nullable Object fieldValue) {
    envMap.put(envName, null);
  }
}
