package bio.overture.dms.infra.util;

import static bio.overture.dms.core.exception.NotFoundException.checkNotFound;
import static bio.overture.dms.core.util.Joiner.COMMA;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static java.util.Arrays.stream;
import static java.util.Comparator.reverseOrder;
import static lombok.AccessLevel.PRIVATE;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

@NoArgsConstructor(access = PRIVATE)
public class FileUtils {

  public static Resource readResourcePath(String filename) throws IOException, URISyntaxException {
    val resource = new DefaultResourceLoader().getResource(filename);
    if (!resource.exists()) {
      throw new IllegalArgumentException(
          format(
              "The resource \"%s\" was not found: %s",
              filename, resource.getFile().getAbsolutePath()));
    }
    return resource;
  }

  public static void checkFileExists(@NonNull Path path) {
    checkNotFound(
        isRegularFile(path), "The file '%s' does not exist or is not a file", path.toString());
  }

  public static void checkDirectoryExists(@NonNull Path path) {
    checkNotFound(
        isDirectory(path), "The path '%s' does not exist or is not a directory", path.toString());
  }

  public static void checkExtensions(@NonNull Path path, @NonNull String... extensions) {
    val found =
        stream(extensions)
            .map(String::toLowerCase)
            .anyMatch(x -> path.getFileName().toString().toLowerCase().endsWith(x));
    checkNotFound(
        found,
        "The path '%s' does not end with any of: [%s]",
        path.toString(),
        COMMA.join(List.of(extensions)));
  }

  @SneakyThrows
  public static void deleteDirectory(@NonNull Path dir) {
    if (exists(dir)) {
      checkDirectoryExists(dir);
      Files.walk(dir).sorted(reverseOrder()).map(Path::toFile).forEach(File::delete);
    }
  }
}
