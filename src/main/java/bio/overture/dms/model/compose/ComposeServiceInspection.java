package bio.overture.dms.model.compose;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ComposeServiceInspection {

  @NonNull private final String containerName;
  @NonNull private final String image;
  @NonNull @Builder.Default private final Map<String, String> environment = new TreeMap<>();
  @NonNull @Builder.Default private final Set<Integer> expose = new HashSet<>();
  @NonNull @Builder.Default private final Map<Integer, Integer> ports = new HashMap<>();
  @NonNull @Builder.Default private final Map<String, String> mounts = new HashMap<>();
}
