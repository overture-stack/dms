package bio.overture.dms.domain.impl.docker;

import bio.overture.dms.domain.ComposeManager;

public class DockerComposeManager implements ComposeManager<DockerComposeObject> {

  @Override
  public void deploy(DockerComposeObject composeObject) {}

  @Override
  public void destroy(DockerComposeObject composeObject, boolean destroyVolumes) {}
}
