package bio.overture.dms.infra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Files {

  public static Resource readResourcePath(String filename) throws IOException, URISyntaxException {
    val resource = new DefaultResourceLoader().getResource(filename);
    if (!resource.exists()) {
      throw new IllegalArgumentException(format("The resource \"%s\" was not found: %s", filename, resource.getFile().getAbsolutePath()));
    }
    return resource;
  }
}
