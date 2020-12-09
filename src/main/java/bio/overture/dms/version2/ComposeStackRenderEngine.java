package bio.overture.dms.version2;

import bio.overture.dms.model.spec.DmsSpec;
import bio.overture.dms.util.ObjectSerializer;
import bio.overture.dms.version2.model.ComposeStack;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import lombok.NonNull;
import lombok.val;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComposeStackRenderEngine {

  private static final String CS_TEMPLATE_LOC = "templates/compose-stack.yaml.vm";

  private final VelocityEngine velocityEngine;
  private final ObjectSerializer jsonSerializer;
  private final ObjectSerializer yamlSerializer;

  @Autowired
  public ComposeStackRenderEngine(
      @NonNull VelocityEngine velocityEngine,
      @NonNull ObjectSerializer jsonSerializer,
      @NonNull ObjectSerializer yamlSerializer) {
    this.velocityEngine = velocityEngine;
    this.jsonSerializer = jsonSerializer;
    this.yamlSerializer = yamlSerializer;
  }

  public ComposeStack render(@NonNull DmsSpec spec) throws IOException {
    val baos = new ByteArrayOutputStream();
    renderYaml(spec, baos);
    val renderedYaml = baos.toString();
    baos.close();
    return yamlSerializer.convertValue(renderedYaml, ComposeStack.class);
  }

  // TODO: check that all the variables in template are resolvable, otherwise throw not found
  // exception
  private void renderYaml(@NonNull DmsSpec spec, @NonNull OutputStream os) throws IOException {
    val template = velocityEngine.getTemplate(CS_TEMPLATE_LOC);
    val ctx = new VelocityContext(yamlSerializer.convertToMap(spec));
    val writer = new OutputStreamWriter(os);
    template.merge(ctx, writer);
    writer.flush();
  }
}
