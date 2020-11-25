package bio.overture.dms.compose;

import bio.overture.dms.model.compose.Compose;
import bio.overture.dms.util.FileUtils;
import bio.overture.dms.util.ObjectSerializer;
import java.io.File;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComposeReader {

  private final ObjectSerializer yamlSerializer;

  @Autowired
  public ComposeReader(@NonNull ObjectSerializer yamlSerializer) {
    this.yamlSerializer = yamlSerializer;
  }

  public Compose readDockerCompose(@NonNull File file) {
    FileUtils.checkExtensions(file.toPath(), "yaml", "yml");
    return yamlSerializer.deserializeFile(file, Compose.class);
  }
}
