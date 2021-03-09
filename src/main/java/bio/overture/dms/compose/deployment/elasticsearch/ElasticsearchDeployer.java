package bio.overture.dms.compose.deployment.elasticsearch;

import static bio.overture.dms.compose.model.ComposeServiceResources.*;

import bio.overture.dms.compose.deployment.DmsComposeManager;
import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.core.Messenger;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.ElasticsearchConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

  public void deploy(boolean runInDocker, @NonNull DmsConfig dmsConfig) {
    serviceDeployer.deploy(dmsConfig, ELASTICSEARCH, true);
    messenger.send("⏳ Waiting for '%s' service to be healthy..", ELASTICSEARCH.toString());
    val host =
        DmsComposeManager.resolveServiceHost(
            ELASTICSEARCH,
            dmsConfig.getClusterRunMode(),
            ElasticsearchConfig.DEFAULT_PORT,
            dmsConfig.getElasticsearch().getHostPort(),
            runInDocker);
    try {
      ServiceDeployer.waitForOk(
          dmsConfig.getElasticsearch().getUrl() + "/_cluster/health?wait_for_status=yellow",
          "elastic:" + dmsConfig.getElasticsearch().getSecurity().getRootPassword(),
          dmsConfig.getHealthCheck().getRetries(),
          dmsConfig.getHealthCheck().getDelaySec());
    } catch (Exception e) {
      messenger.send("❌ Health check failed for Elasticsearch");
      throw e;
    }
    messenger.send("\uD83C\uDFC1️ Deployment for '%s' finished ", ELASTICSEARCH.toString());
  }
}
