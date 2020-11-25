package bio.overture.dms.infra.service;

import bio.overture.dms.infra.model.DockerCompose;
import bio.overture.dms.infra.util.FileUtils;
import bio.overture.dms.infra.util.JsonProcessor;
import java.io.File;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DCReader {

  private final JsonProcessor yamlProcessor;

  @Autowired
  public DCReader(@NonNull JsonProcessor yamlProcessor) {
    this.yamlProcessor = yamlProcessor;
  }

  public DockerCompose readDockerCompose(@NonNull File file) {
    FileUtils.checkExtensions(file.toPath(), "yaml", "yml");
    return yamlProcessor.readValue(file, DockerCompose.class);
  }
}
