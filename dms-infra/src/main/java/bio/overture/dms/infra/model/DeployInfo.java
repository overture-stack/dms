package bio.overture.dms.infra.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Value
@Builder
public class DeployInfo {

  @NonNull private final String containerName;
  @NonNull private final String image;
  @NonNull @Builder.Default private final Map<String, String> environment = new TreeMap<>();
  @NonNull @Builder.Default private final Set<Integer> expose = new HashSet<>();
  @NonNull @Builder.Default private final Map<Integer, Integer> ports = new HashMap<>();
  @NonNull @Builder.Default private final Map<String, String> mounts = new HashMap<>();

}
