package bio.overture.dms.compose.model;

import bio.overture.dms.compose.model.stack.ComposeService;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public enum ComposeServiceResources {
  EGO_API("ego-api"),
  EGO_DB("ego-db"),
  EGO_UI("ego-ui");

  private static final Path RESOURCES_DIR = Paths.get("src/main/resources");
  private static final Path COMPOSE_STACK_TEMPLATE_DIR = RESOURCES_DIR.resolve("templates/stack/");

  private final String text;

  @Override
  public String toString() {
    return text;
  }

  public static Stream<ComposeServiceResources> stream(){
    return Arrays.stream(values());
  }

  public Path getResourcePath() {
   return COMPOSE_STACK_TEMPLATE_DIR.resolve(getFilename());
  }

  public Path getVelocityPath(){
    return RESOURCES_DIR.relativize(getResourcePath());
  }

  private String getFilename() {
    return toString() + ".yaml.vm";
  }

}
