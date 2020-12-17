package bio.overture.dms.compose.service;

import static bio.overture.dms.core.util.FileUtils.readResourcePath;

import bio.overture.dms.core.util.ObjectSerializer;
import com.github.dockerjava.api.model.SwarmSpec;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SwarmSpecService {

  // TODO: move this into a directory called static or swarm. This is not a template file
  private static final String INIT_SWARM_SPEC_LOC = "/initSwarm.yaml";
  private final ObjectSerializer yamlSerializer;

  @Autowired
  public SwarmSpecService(@NonNull @Qualifier("yamlSerializer") ObjectSerializer yamlSerializer) {
    this.yamlSerializer = yamlSerializer;
  }

  @SneakyThrows
  public SwarmSpec getInitSwarmSpec() {
    val file = readResourcePath(INIT_SWARM_SPEC_LOC).getFile();
    return yamlSerializer.deserializeFile(file, SwarmSpec.class);
  }
}
