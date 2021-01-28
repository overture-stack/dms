package bio.overture.dms.compose.deployment.maestro;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static bio.overture.dms.compose.model.ComposeServiceResources.MAESTRO;

@Slf4j
@Component
public class MaestroDeployer {
  private final ServiceDeployer serviceDeployer;

  public MaestroDeployer(ServiceDeployer serviceDeployer) {
    this.serviceDeployer = serviceDeployer;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    serviceDeployer.deploy(dmsConfig, MAESTRO, true);
  }

}
