package bio.overture.dms.compose.manager;

import static java.util.stream.Collectors.toUnmodifiableList;

import bio.overture.dms.compose.manager.deployer.EgoApiDbDeployer;
import bio.overture.dms.compose.manager.deployer.EgoUiDeployer;
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

/**
 * Decorator that converts a DmsConfig object to a ComposeStack object before calling the internal
 * ComposeManager implementation
 */
@Slf4j
@Component
public class DmsComposeManager2 implements ComposeManager<DmsConfig> {

  private final ExecutorService executorService;
  private final SwarmService swarmService;
  private final EgoApiDbDeployer egoApiDbDeployer;
  private final EgoUiDeployer egoUiDeployer;

  @Autowired
  public DmsComposeManager2(
      @NonNull ExecutorService executorService,
      @NonNull SwarmService swarmService,
      @NonNull EgoApiDbDeployer egoApiDbDeployer,
      @NonNull EgoUiDeployer egoUiDeployer) {
    this.executorService = executorService;
    this.swarmService = swarmService;
    this.egoApiDbDeployer = egoApiDbDeployer;
    this.egoUiDeployer = egoUiDeployer;
  }

  @Override
  public void deploy(@NonNull DmsConfig dmsConfig) {
    swarmService.initializeSwarm();
    val out =
        CompletableFuture.runAsync(() -> egoApiDbDeployer.deploy(dmsConfig), executorService)
            .thenRunAsync(() -> egoUiDeployer.deploy(dmsConfig), executorService);
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
