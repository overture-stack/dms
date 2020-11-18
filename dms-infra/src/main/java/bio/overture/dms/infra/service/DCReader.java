package bio.overture.dms.infra.service;

import bio.overture.dms.infra.model.DockerCompose;
import bio.overture.dms.infra.util.JsonProcessor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

import static bio.overture.dms.infra.docker.NotFoundException.checkNotFound;
import static bio.overture.dms.infra.util.FileUtils.checkExtensions;

@Component
public class DCReader {

  private final JsonProcessor yamlProcessor;

  @Autowired
  public DCReader(@NonNull JsonProcessor yamlProcessor) {
    this.yamlProcessor = yamlProcessor;
  }

  public DockerCompose readDockerCompose(@NonNull File file){
    checkExtensions(file.toPath(),"yaml", "yml");
    return yamlProcessor.readValue(file, DockerCompose.class);
  }

}
