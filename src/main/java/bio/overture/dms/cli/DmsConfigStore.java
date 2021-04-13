package bio.overture.dms.cli;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.readString;

import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.swarm.properties.DockerProperties;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
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

  private DockerProperties properties;

  @Autowired
  public DmsConfigStore(
      @NonNull ObjectSerializer yamlSerializer, @NonNull DockerProperties properties) {
    this.yamlSerializer = yamlSerializer;
    this.properties = properties;
  }

  @SneakyThrows
  public Path getDmsConfigFilePath() {
    val userDir = Paths.get(System.getProperty("user.home"));
    val dmsDir = userDir.resolve(".dms");
    createDirectories(dmsDir);
    return dmsDir.resolve(CONFIG_FILE_NAME);
  }

  @SneakyThrows
  public Optional<String> findStoredConfigContents() {
    val file = getDmsConfigFilePath();
    if (isRegularFile(file)) {
      return Optional.of(readString(file));
    }
    return Optional.empty();
  }

  public Optional<DmsConfig> findStoredConfig() {
    val file = getDmsConfigFilePath();
    if (isRegularFile(file)) {
      return Optional.of(
          yamlSerializer.deserializeToObject(getDmsConfigFilePath().toFile(), DmsConfig.class));
    }
    return Optional.empty();
  }

  private void save(DmsConfig dmsConfig) {
    yamlSerializer.serializeToFile(dmsConfig, getDmsConfigFilePath().toFile());
  }

  public void apply(Function<DmsConfig, DmsConfig> transformation) {
    val storedDmsConfig =
        findStoredConfig()
            // this is only needed when running locally (make sure to set the application properties
            // correctly)
            // in real scenarios it will be passed from the dms-docker script which is read from the
            // initial
            // config file.
            .orElse(
                yamlSerializer.deserializeToObject(
                    "version: " + properties.getTag(), DmsConfig.class));
    val dmsConfig = transformation.apply(storedDmsConfig);
    backupExistingConfig(storedDmsConfig);
    save(dmsConfig);
  }

  @SneakyThrows
  private void backupExistingConfig(DmsConfig storedDmsConfig) {
    val df = new SimpleDateFormat("yyyyMMddHHmm");
    File f = new File(getDmsConfigFilePath() + ".backup-" + df.format(new Date()));
    f.createNewFile();
    yamlSerializer.serializeToFile(storedDmsConfig, f);
  }
}
