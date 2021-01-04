package bio.overture.dms.swarm.service;

import static bio.overture.dms.core.util.FileUtils.readResourceStream;

import bio.overture.dms.core.util.ObjectSerializer;
import com.github.dockerjava.api.model.SwarmSpec;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SwarmSpecService {

  //    private static final String INIT_SWARM_SPEC_LOC = "classpath:swarm/initSwarm.yaml";
  private static final String INIT_SWARM_SPEC_LOC = "swarm/initSwarm.yaml";
  private final ObjectSerializer yamlSerializer;

  @Autowired
  public SwarmSpecService(@NonNull @Qualifier("yamlSerializer") ObjectSerializer yamlSerializer) {
    this.yamlSerializer = yamlSerializer;
  }

  @SneakyThrows
  public SwarmSpec getInitSwarmSpec() {
    return yamlSerializer.deserializeToObject(
        readResourceStream(INIT_SWARM_SPEC_LOC), SwarmSpec.class);
  }
}
