package bio.overture.dms.compose.deployment.elasticsearch;

import static bio.overture.dms.compose.model.ComposeServiceResources.*;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.core.Messenger;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.ElasticsearchConfig;
import java.net.URI;
import java.net.URL;
import lombok.NonNull;
import lombok.SneakyThrows;
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

  @SneakyThrows
  public void deploy(boolean runInDocker, @NonNull DmsConfig dmsConfig) {
    serviceDeployer.deploy(dmsConfig, ELASTICSEARCH, true);
    messenger.send("⏳ Waiting for '%s' service to be healthy..", ELASTICSEARCH.toString());

    URL baseUrl = dmsConfig.getElasticsearch().getUrl();
    if (runInDocker) {
      baseUrl =
          new URI("http://" + ELASTICSEARCH.toString() + ":" + ElasticsearchConfig.DEFAULT_PORT)
              .toURL();
    }
    try {
      ServiceDeployer.waitForOk(
          baseUrl + "/_cluster/health?wait_for_status=yellow",
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
