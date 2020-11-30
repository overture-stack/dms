package bio.overture.dms.domain.impl.docker;

import bio.overture.dms.domain.SpecPersistence;
import java.io.File;

public class DockerSpecPersistence implements SpecPersistence<DockerDMSSpec> {

  @Override
  public void writeToFile(DockerDMSSpec spec, File f) {}

  @Override
  public DockerDMSSpec readFromFile(File f) {
    return null;
  }
}
