package bio.overture.dms.compose.manager;

import bio.overture.dms.compose.model.ComposeServiceResources;
import bio.overture.dms.compose.model.stack.ComposeStack;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Deprecated
@Component
public class ComposeStackRenderEngine {

  private final ComposeServiceRenderEngine composeServiceRenderEngine;

  @Autowired
  public ComposeStackRenderEngine(@NonNull ComposeServiceRenderEngine composeServiceRenderEngine) {
    this.composeServiceRenderEngine = composeServiceRenderEngine;
  }

  @SneakyThrows
  public ComposeStack render(@NonNull DmsConfig dmsConfig) {
    val cs = new ComposeStack();
    ComposeServiceResources.stream()
        .map(r -> composeServiceRenderEngine.render(dmsConfig, r))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(x -> cs.getServices().add(x));
    return cs;
  }
}
