package bio.overture.dms.cli;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.isRegularFile;

import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.util.ObjectSerializer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DmsConfigStore {

  /** Constants */
  public static final String CONFIG_FILE_NAME = "config.yaml";

  /** Dependencies */
  private final ObjectSerializer yamlSerializer;

  @Autowired
  public DmsConfigStore(@NonNull ObjectSerializer yamlSerializer) {
    this.yamlSerializer = yamlSerializer;
  }

  @SneakyThrows
  public Path getDmsConfigFilePath() {
    val userDir = Paths.get(System.getProperty("user.home"));
    val dmsDir = userDir.resolve(".dms");
    createDirectories(dmsDir);
    return dmsDir.resolve(CONFIG_FILE_NAME);
  }

  public Optional<DmsConfig> findStoredConfig() {
    val file = getDmsConfigFilePath();
    if (isRegularFile(file)) {
      return Optional.of(
          yamlSerializer.deserializeFile(getDmsConfigFilePath().toFile(), DmsConfig.class));
    }
    return Optional.empty();
  }

  public void save(DmsConfig dmsConfig) {
    yamlSerializer.serializeToFile(dmsConfig, getDmsConfigFilePath().toFile());
  }

  public void apply(Function<DmsConfig, DmsConfig> transformation) {
    val storedDmsConfig = findStoredConfig().orElse(null);

    // TODO: ideally, this is how a null is treated
    //    findStoredConfig()
    //        .map(transformation::apply)
    //        .ifPresent(this::save);

    val dmsConfig = transformation.apply(storedDmsConfig);
    save(dmsConfig);
  }
}
