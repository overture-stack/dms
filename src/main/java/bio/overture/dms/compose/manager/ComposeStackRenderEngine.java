package bio.overture.dms.compose.manager;

import static java.nio.file.Files.walk;

import bio.overture.dms.compose.model.stack.ComposeService;
import bio.overture.dms.compose.model.stack.ComposeStack;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.util.ObjectSerializer;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComposeStackRenderEngine {

  private static final Path RESOURCES_DIR = Paths.get("src/main/resources");
  private static final Path COMPOSE_STACK_TEMPLATE_DIR = RESOURCES_DIR.resolve("templates/stack/");

  private final VelocityEngine velocityEngine;
  private final ObjectSerializer velocitySerializer;
  private final ObjectSerializer yamlSerializer;

  @Autowired
  public ComposeStackRenderEngine(
      @NonNull VelocityEngine velocityEngine,
      @NonNull ObjectSerializer velocitySerializer,
      @NonNull ObjectSerializer yamlSerializer) {
    this.velocityEngine = velocityEngine;
    this.velocitySerializer = velocitySerializer;
    this.yamlSerializer = yamlSerializer;
  }

  @SneakyThrows
  public ComposeStack render(@NonNull DmsConfig dmsConfig) {
    val cs = new ComposeStack();
    walk(COMPOSE_STACK_TEMPLATE_DIR, 1)
        .filter(Files::isRegularFile)
        .map(f -> renderComposeService(dmsConfig, f))
        .forEach(x -> cs.getServices().add(x));
    return cs;
  }

  @SneakyThrows
  private ComposeService renderComposeService(DmsConfig spec, Path f) {
    val baos = new ByteArrayOutputStream();
    renderYaml(spec, baos, f);
    val renderedYaml = baos.toString();
    baos.close();
    return yamlSerializer.convertValue(renderedYaml, ComposeService.class);
  }

  @SneakyThrows
  private void renderYaml(
      @NonNull DmsConfig spec, @NonNull OutputStream os, @NonNull Path filepath) {
    val vmfile = RESOURCES_DIR.relativize(filepath).toString();
    val template = velocityEngine.getTemplate(vmfile);
    val ctx = new VelocityContext(velocitySerializer.convertToMap(spec));
    val writer = new OutputStreamWriter(os);
    template.merge(ctx, writer);
    writer.flush();
  }
}
