package bio.overture.dms.compose.manager;

import static java.nio.file.Files.isRegularFile;
import static java.util.stream.Collectors.toUnmodifiableMap;

import bio.overture.dms.compose.model.ComposeServiceResources;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.util.ObjectSerializer;
import com.github.dockerjava.api.model.ServiceSpec;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceSpecRenderEngine {

  /** Constants */
  private static final String COMPOSE_SERVICE_RESOURCES = "composeServiceResources";

  private static final String DMS_CONFIG = "dmsConfig";

  /** Dependencies */
  private final VelocityEngine velocityEngine;

  private final ObjectSerializer yamlSerializer;

  @Autowired
  public ServiceSpecRenderEngine(
      @NonNull VelocityEngine velocityEngine, @NonNull ObjectSerializer yamlSerializer) {
    this.velocityEngine = velocityEngine;
    this.yamlSerializer = yamlSerializer;
  }

  @SneakyThrows
  public Optional<ServiceSpec> render(
      @NonNull DmsConfig dmsConfig, @NonNull ComposeServiceResources composeServiceResource) {
    if (isRegularFile(composeServiceResource.getResourcePath())) {
      return Optional.of(renderServiceSpec(dmsConfig, composeServiceResource));
    }
    return Optional.empty();
  }

  @SneakyThrows
  private ServiceSpec renderServiceSpec(
      DmsConfig spec, ComposeServiceResources composeServiceResource) {
    val baos = new ByteArrayOutputStream();
    renderYaml(spec, baos, composeServiceResource);
    val renderedYaml = baos.toString();
    baos.close();
    return yamlSerializer.convertValue(renderedYaml, ServiceSpec.class);
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
    val out = new HashMap<String, Object>();
    out.put(DMS_CONFIG, dmsConfig);
    val composeServiceResourceMap =
        ComposeServiceResources.stream()
            .collect(toUnmodifiableMap(Enum::name, ComposeServiceResources::toString));
    out.put(COMPOSE_SERVICE_RESOURCES, composeServiceResourceMap);
    return Map.copyOf(out);
  }
}
