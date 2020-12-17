package bio.overture.dms.domain;

import static bio.overture.dms.core.util.FileUtils.checkFileExists;
import static java.lang.System.getProperty;
import static java.nio.file.Files.createDirectories;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public interface SpecPersistence<S> {

  void writeToFile(S spec, File f);

  S readFromFile(File f);

  default Path getDmsHomeDirpath() {
    return Paths.get(getProperty("user.home")).resolve(".dms");
  }

  default Path getSpecFilepath() {
    return getDmsHomeDirpath().resolve("spec.yaml");
  }

  default S readFromHome() {
    val specFile = getSpecFilepath();
    checkFileExists(specFile);
    return readFromFile(specFile.toFile());
  }

  default void writeToHome(@NonNull S spec) {
    val specFile = getSpecFilepath();
    provisionHomeDir();
    writeToFile(spec, specFile.toFile());
  }

  @SneakyThrows
  private void provisionHomeDir() {
    createDirectories(getDmsHomeDirpath());
  }
}
