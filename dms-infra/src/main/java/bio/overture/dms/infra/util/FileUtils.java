package bio.overture.dms.infra.util;

import bio.overture.dms.core.Joiner;
import bio.overture.dms.infra.docker.NotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static bio.overture.dms.core.Joiner.COMMA;
import static bio.overture.dms.infra.docker.NotFoundException.checkNotFound;
import static java.lang.String.format;
import static java.nio.file.Files.isRegularFile;
import static java.util.Arrays.stream;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class FileUtils {

  public static Resource readResourcePath(String filename) throws IOException, URISyntaxException {
    val resource = new DefaultResourceLoader().getResource(filename);
    if (!resource.exists()) {
      throw new IllegalArgumentException(format("The resource \"%s\" was not found: %s", filename, resource.getFile().getAbsolutePath()));
    }
    return resource;
  }

  public static void checkFileExists(@NonNull Path path){
    checkNotFound(isRegularFile(path), "The file '%s' does not exist", path.toString());
  }

  public static void checkExtensions(@NonNull Path path, @NonNull String ... extensions) {
    val found = stream(extensions)
        .map(String::toLowerCase)
        .anyMatch(x -> path.getFileName().toString().toLowerCase().endsWith(x));
    checkNotFound(
        found,
        "The path '%s' does not end with any of: [%s]",
        path.toString(),
        COMMA.join(List.of(extensions)));
  }
}
