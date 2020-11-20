package bio.overture.dms.infra.template;

import bio.overture.dms.infra.model.DockerCompose;
import bio.overture.dms.infra.spec.DmsSpec;
import bio.overture.dms.infra.util.JsonProcessor;
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

@Component
public class DCRenderer {

  private static final String DC_TEMPLATE_LOC = "templates/docker-compose.yaml.vm";

  private final VelocityEngine velocityEngine;
  private final JsonProcessor jsonProcessor;
  private final JsonProcessor yamlProcessor;

  @Autowired
  public DCRenderer(@NonNull VelocityEngine velocityEngine, @NonNull JsonProcessor jsonProcessor,
      @NonNull JsonProcessor yamlProcessor) {
    this.velocityEngine = velocityEngine;
    this.jsonProcessor = jsonProcessor;
    this.yamlProcessor = yamlProcessor;
  }

  @SneakyThrows
  public DockerCompose render(@NonNull DmsSpec spec){
    val baos = new ByteArrayOutputStream();
    renderYaml(spec, baos);
    val renderedYaml = baos.toString();
    baos.close();
    return yamlProcessor.convertValue(renderedYaml, DockerCompose.class);
  }

  @SneakyThrows
  private void renderYaml(@NonNull DmsSpec spec, @NonNull OutputStream os){
    val template = velocityEngine.getTemplate(DC_TEMPLATE_LOC);
    val ctx = new VelocityContext(jsonProcessor.convertToMap(spec));
    val writer = new OutputStreamWriter(os);
    template.merge(ctx, writer );
    writer.flush();
  }


}
