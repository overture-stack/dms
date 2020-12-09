package bio.overture.dms.domain.compose;

import static java.lang.String.format;
import static java.util.Arrays.stream;

import lombok.NonNull;

public enum DependencyCondition {
  SERVICE_STARTED,
  SERVICE_HEALTHY;

  @Override
  public String toString() {
    return name().toLowerCase();
  }

  public static DependencyCondition resolveDependencyCondition(@NonNull String condition) {
    return stream(values())
        .filter(x -> x.toString().equals(condition))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    format("The dependency condition '%s' cannot be resolved", condition)));
  }
}
