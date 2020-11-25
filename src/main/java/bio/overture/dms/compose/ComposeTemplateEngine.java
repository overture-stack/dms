package bio.overture.dms.compose;

import bio.overture.dms.model.compose.Compose;
import bio.overture.dms.model.spec.DmsSpec;
import bio.overture.dms.util.ObjectSerializer;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComposeTemplateEngine {

  private static final String DC_TEMPLATE_LOC = "templates/docker-compose.yaml.vm";

  private final VelocityEngine velocityEngine;
  private final ObjectSerializer jsonSerializer;
  private final ObjectSerializer yamlSerializer;

  @Autowired
  public ComposeTemplateEngine(
      @NonNull VelocityEngine velocityEngine,
      @NonNull ObjectSerializer jsonSerializer,
      @NonNull ObjectSerializer yamlSerializer) {
    this.velocityEngine = velocityEngine;
    this.jsonSerializer = jsonSerializer;
    this.yamlSerializer = yamlSerializer;
  }

  @SneakyThrows
  public Compose render(@NonNull DmsSpec spec) {
    val baos = new ByteArrayOutputStream();
    renderYaml(spec, baos);
    val renderedYaml = baos.toString();
    baos.close();
    return yamlSerializer.convertValue(renderedYaml, Compose.class);
  }

  // TODO: check that all the variables in template are resolvable, otherwise throw not found
  // exception
  @SneakyThrows
  private void renderYaml(@NonNull DmsSpec spec, @NonNull OutputStream os) {
    val template = velocityEngine.getTemplate(DC_TEMPLATE_LOC);
    val ctx = new VelocityContext(jsonSerializer.convertToMap(spec));
    val writer = new OutputStreamWriter(os);
    template.merge(ctx, writer);
    writer.flush();
  }
}
