package bio.overture.dms.compose.model;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public enum ComposeServiceResources {
  EGO_API("ego-api"),
  EGO_DB("ego-db"),
  EGO_UI("ego-ui");

  private static final Path RESOURCES_DIR = Paths.get("src/main/resources");
  private static final Path COMPOSE_STACK_TEMPLATE_DIR = RESOURCES_DIR.resolve("templates/servicespec/");

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
