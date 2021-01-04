package bio.overture.dms.compose.manager;

import static bio.overture.dms.core.util.Exceptions.checkState;
import static java.nio.file.Files.isRegularFile;

import bio.overture.dms.compose.model.ComposeServiceResources;
import bio.overture.dms.compose.model.stack.ComposeService;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.util.ObjectSerializer;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Deprecated
@Component
public class ComposeServiceRenderEngine {

  /** Constants */
  private static final String COMPOSE_SERVICE_RESOURCES = "composeServiceResources";

  /** Dependencies */
  private final VelocityEngine velocityEngine;

  private final ObjectSerializer velocitySerializer;
  private final ObjectSerializer yamlSerializer;

  @Autowired
  public ComposeServiceRenderEngine(
      @NonNull VelocityEngine velocityEngine,
      @NonNull ObjectSerializer velocitySerializer,
      @NonNull ObjectSerializer yamlSerializer) {
    this.velocityEngine = velocityEngine;
    this.velocitySerializer = velocitySerializer;
    this.yamlSerializer = yamlSerializer;
  }

  @SneakyThrows
  public Optional<ComposeService> render(
      @NonNull DmsConfig dmsConfig, @NonNull ComposeServiceResources composeServiceResource) {
    if (isRegularFile(composeServiceResource.getResourcePath())) {
      return Optional.of(renderComposeService(dmsConfig, composeServiceResource));
    }
    return Optional.empty();
  }

  @SneakyThrows
  private ComposeService renderComposeService(
      DmsConfig spec, ComposeServiceResources composeServiceResource) {
    val baos = new ByteArrayOutputStream();
    renderYaml(spec, baos, composeServiceResource);
    val renderedYaml = baos.toString();
    baos.close();
    return yamlSerializer.convertValue(renderedYaml, ComposeService.class);
  }

  @SneakyThrows
  private void renderYaml(
      @NonNull DmsConfig dmsConfig,
      @NonNull OutputStream os,
      @NonNull ComposeServiceResources composeServiceResource) {
    val vmFile = composeServiceResource.getVelocityPath().toString();
    val template = velocityEngine.getTemplate(vmFile);
    val ctx = new VelocityContext(resolveContextMap(dmsConfig));
    val writer = new OutputStreamWriter(os);
    template.merge(ctx, writer);
    writer.flush();
  }

  private Map<String, Object> resolveContextMap(DmsConfig dmsConfig) {
    val map = velocitySerializer.convertToMap(dmsConfig);
    checkState(
        !map.containsKey(COMPOSE_SERVICE_RESOURCES),
        "Duplicate entry found for key '%s'",
        COMPOSE_SERVICE_RESOURCES);
    return map;
  }
}
