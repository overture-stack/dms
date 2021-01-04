package bio.overture.dms.compose.manager.deployer;

import bio.overture.dms.compose.manager.ServiceDeployer;
import bio.overture.dms.compose.manager.ServiceDeployer.DeployTypes;
import bio.overture.dms.compose.manager.ServiceSpecRenderEngine;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.ego.EgoClientFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_UI;

@Slf4j
@Component
public class EgoUiDeployer {

  private final ServiceSpecRenderEngine serviceSpecRenderEngine;
  private final ServiceDeployer serviceDeployer;
  private final EgoClientFactory egoClientFactory;

  @Autowired
  public EgoUiDeployer( @NonNull ServiceSpecRenderEngine serviceSpecRenderEngine,
      @NonNull ServiceDeployer serviceDeployer,
      @NonNull EgoClientFactory egoClientFactory){
    this.serviceSpecRenderEngine = serviceSpecRenderEngine;
    this.serviceDeployer = serviceDeployer;
    this.egoClientFactory = egoClientFactory;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    attemptInitialization(dmsConfig.getEgo());
    val uiDeployType = deployUi(dmsConfig);
  }

  private DeployTypes deployUi(DmsConfig dmsConfig) {
    val uiServiceSpec = serviceSpecRenderEngine.render(dmsConfig, EGO_UI)
        .orElseThrow();
    val dbDeployType = serviceDeployer.process(uiServiceSpec);
    serviceDeployer.waitForServiceRunning(uiServiceSpec);
    return dbDeployType;
  }

  private void attemptInitialization (EgoConfig egoConfig) {
    val dmsEgoClient = egoClientFactory.buildAuthDmsEgoClient(egoConfig);
    // TODO: implement properly
    log.info("DOOOOOOOOOOOOOOOOOOOO STUFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
  }


}
