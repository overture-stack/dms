package bio.overture.dms.domain.impl.docker;

import bio.overture.dms.domain.ComposeObject;
import java.util.Set;

public class DockerComposeObject implements ComposeObject<DockerComposeItem> {

  @Override
  public String getVersion() {
    return null;
  }

  @Override
  public Set<DockerComposeItem> getComposeItems() {
    return null;
  }
}
