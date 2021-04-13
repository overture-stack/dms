package bio.overture.dms.cli.util;

import bio.overture.dms.swarm.properties.DockerProperties;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.IVersionProvider;

@Component
public class VersionProvider implements IVersionProvider {

  private final DockerProperties dockerProperties;

  @Autowired
  public VersionProvider(@NonNull DockerProperties dockerProperties) {
    this.dockerProperties = dockerProperties;
  }

  @Override
  public String[] getVersion() throws Exception {
    return new String[] {dockerProperties.getTag()};
  }
}
