package bio.overture.dms.compose.manager;

import bio.overture.dms.compose.model.ComposeServiceResources;
import bio.overture.dms.compose.model.stack.ComposeService;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.util.ObjectSerializer;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;

import static java.nio.file.Files.isRegularFile;

@Component
public class ComposeServiceRenderEngine {

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
  public Optional<ComposeService> render(@NonNull DmsConfig dmsConfig, @NonNull ComposeServiceResources composeServiceResource) {
    if (isRegularFile(composeServiceResource.getResourcePath())){
      return Optional.of(renderComposeService(dmsConfig, composeServiceResource));
    }
    return Optional.empty();
  }

  @SneakyThrows
  private ComposeService renderComposeService(DmsConfig spec, ComposeServiceResources composeServiceResource) {
    val baos = new ByteArrayOutputStream();
    renderYaml(spec, baos, composeServiceResource);
    val renderedYaml = baos.toString();
    baos.close();
    return yamlSerializer.convertValue(renderedYaml, ComposeService.class);
  }

  @SneakyThrows
  private void renderYaml(
      @NonNull DmsConfig spec, @NonNull OutputStream os, @NonNull ComposeServiceResources composeServiceResource) {
    val vmfile = composeServiceResource.getVelocityPath().toString();
    val template = velocityEngine.getTemplate(vmfile);
    val ctx = new VelocityContext(velocitySerializer.convertToMap(spec));
    val writer = new OutputStreamWriter(os);
    template.merge(ctx, writer);
    writer.flush();
  }

}
