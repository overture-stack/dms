package bio.overture.dms.compose.deployment.elasticsearch;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static bio.overture.dms.compose.model.ComposeServiceResources.ELASTICSEARCH;

@Slf4j
@Component
public class ElasticsearchDeployer {
  private final ServiceDeployer serviceDeployer;

  public ElasticsearchDeployer(ServiceDeployer serviceDeployer) {
    this.serviceDeployer = serviceDeployer;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    serviceDeployer.deploy(dmsConfig, ELASTICSEARCH, true);
  }

}
