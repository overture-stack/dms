package bio.overture.dms.docker.env;

import bio.overture.dms.core.Nullable;
import lombok.NonNull;

import java.util.Map;


/**
 * Implementation of a env field visitor that persists any environment variable to the map for dumping purposes
 */
public class DumpEnvFieldVisitor implements EnvFieldVisitor {

  @Override
  public void visit(@NonNull Map<String, String> envMap, @NonNull String envName, @Nullable Object fieldValue) {
    envMap.put(envName, null);
  }

}
