package bio.overture.dms.compose.model;

import static bio.overture.dms.core.util.FileUtils.isResourceExist;
import static lombok.AccessLevel.PRIVATE;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PRIVATE)
public enum ComposeServiceResources {
  EGO_API("ego-api"),
  EGO_DB("ego-db"),
  EGO_UI("ego-ui"),
  SONG_DB("song-db"),
  SONG_API("song-api"),
  SCORE_API("score-api"),
  MINIO_API("minio-api"),
  ELASTICSEARCH("elasticsearch"),
  MAESTRO("maestro"),
  ARRANGER_SERVER("arranger-server"),
  ARRANGER_UI("arranger-ui"),
  DMS_UI("dms-ui"),
  ;

  private static final Path TEMPLATE_DIR = Paths.get("templates/servicespec");

  private final String text;

  @Override
  public String toString() {
    return text;
  }

  public static Stream<ComposeServiceResources> stream() {
    return Arrays.stream(values());
  }

  public boolean exists() {
    return isResourceExist(getVelocityPath().toString());
  }

  public Path getVelocityPath() {
    return TEMPLATE_DIR.resolve(getFilename());
  }

  private String getFilename() {
    return toString() + ".yaml.vm";
  }
}
