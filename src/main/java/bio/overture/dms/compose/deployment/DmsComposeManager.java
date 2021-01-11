package bio.overture.dms.compose.deployment;

import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_UI;
import static java.util.stream.Collectors.toUnmodifiableList;

import bio.overture.dms.compose.deployment.ego.EgoApiDbDeployer;
import bio.overture.dms.compose.model.ComposeServiceResources;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.swarm.service.SwarmService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Composes DMS services in a swarm cluster */
@Slf4j
@Component
public class DmsComposeManager implements ComposeManager<DmsConfig> {

  private final ExecutorService executorService;
  private final SwarmService swarmService;
  private final EgoApiDbDeployer egoApiDbDeployer;
  private final ServiceDeployer serviceDeployer;

  @Autowired
  public DmsComposeManager(
      @NonNull ExecutorService executorService,
      @NonNull SwarmService swarmService,
      @NonNull EgoApiDbDeployer egoApiDbDeployer,
      @NonNull ServiceDeployer serviceDeployer) {
    this.executorService = executorService;
    this.swarmService = swarmService;
    this.egoApiDbDeployer = egoApiDbDeployer;
    this.serviceDeployer = serviceDeployer;
  }

  @Override
  public void deploy(@NonNull DmsConfig dmsConfig) {
    swarmService.initializeSwarm();
    swarmService.getOrCreateNetwork(dmsConfig.getNetwork());

    val out =
        CompletableFuture.runAsync(() -> egoApiDbDeployer.deploy(dmsConfig), executorService)
            .thenRunAsync(() -> serviceDeployer.deploy(dmsConfig, EGO_UI, true), executorService);
    out.join();
    log.info("sdfdsf");
  }

  @Override
  public void destroy(@NonNull DmsConfig dmsConfig, boolean destroyVolumes) {
    val serviceNames =
        ComposeServiceResources.stream()
            .map(ComposeServiceResources::toString)
            .collect(toUnmodifiableList());
    swarmService.deleteServices(serviceNames, destroyVolumes);
  }
}
