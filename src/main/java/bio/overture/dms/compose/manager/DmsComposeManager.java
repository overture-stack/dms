package bio.overture.dms.compose.manager;

import bio.overture.dms.compose.model.stack.ComposeStack;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Decorator that converts a DmsConfig object to a ComposeStack object before calling the internal
 * ComposeManager implementation
 */
@Deprecated
@Component
public class DmsComposeManager implements ComposeManager<DmsConfig> {

  private final ComposeStackRenderEngine composeStackRenderEngine;
  private final ComposeManager<ComposeStack> internalManager;

  @Autowired
  public DmsComposeManager(
      @NonNull ComposeStackRenderEngine composeStackRenderEngine,
      @NonNull ComposeStackManager internalManager) {
    this.composeStackRenderEngine = composeStackRenderEngine;
    this.internalManager = internalManager;
  }

  @Override
  public void deploy(@NonNull DmsConfig dmsConfig) {
    internalManager.deploy(composeStackRenderEngine.render(dmsConfig));
  }

  @Override
  public void destroy(@NonNull DmsConfig dmsConfig, boolean destroyVolumes) {
    internalManager.destroy(composeStackRenderEngine.render(dmsConfig), destroyVolumes);
  }
}
