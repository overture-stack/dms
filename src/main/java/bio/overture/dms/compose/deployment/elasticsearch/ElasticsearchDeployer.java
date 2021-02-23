package bio.overture.dms.compose.deployment.elasticsearch;

import static bio.overture.dms.compose.model.ComposeServiceResources.*;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.core.Messenger;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.ElasticsearchConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ElasticsearchDeployer {

  /** Dependencies */
  private final ServiceDeployer serviceDeployer;

  private final Messenger messenger;

  @Autowired
  public ElasticsearchDeployer(
      @NonNull ServiceDeployer serviceDeployer, @NonNull Messenger messenger) {
    this.serviceDeployer = serviceDeployer;
    this.messenger = messenger;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    serviceDeployer.deploy(dmsConfig, ELASTICSEARCH, true);
    messenger.send("⏳ Waiting for '%s' service to be healthy..", ELASTICSEARCH.toString());
    ServiceDeployer.waitForOk(
        "http://"
            + ELASTICSEARCH.toString()
            + ":"
            + ElasticsearchConfig.DEFAULT_PORT
            + "/_cluster/health?wait_for_status=yellow",
        "elastic:" + dmsConfig.getElasticsearch().getSecurity().getRootPassword());

    messenger.send("\uD83C\uDFC1️ Deployment for '%s' finished ", ELASTICSEARCH.toString());
  }
}
