package bio.overture.dms.compose.manager;

import bio.overture.dms.compose.manager.deployer.EgoApiDbDeployer;
import bio.overture.dms.compose.manager.deployer.EgoUiDeployer;
import bio.overture.dms.compose.model.ComposeServiceResources;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.util.CollectionUtils;
import bio.overture.dms.swarm.service.SwarmService;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.nio.file.Files.walk;
import static java.time.Duration.ofSeconds;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Decorator that converts a DmsConfig object to a ComposeStack object before calling the internal
 * ComposeManager implementation
 */
@Slf4j
@Component
public class DmsComposeManager2 implements ComposeManager<DmsConfig> {

  private static final Path RESOURCES_DIR = Paths.get("src/main/resources");
  private static final Path COMPOSE_STACK_TEMPLATE_DIR = RESOURCES_DIR.resolve("templates/stack/");

  private final ExecutorService executorService;
  private final SwarmService swarmService;
  private final EgoApiDbDeployer egoApiDbDeployer;
  private final EgoUiDeployer egoUiDeployer;

  @Autowired
  public DmsComposeManager2(
      @NonNull ExecutorService executorService,
      @NonNull SwarmService swarmService,
      @NonNull EgoApiDbDeployer egoApiDbDeployer,
      @NonNull EgoUiDeployer egoUiDeployer){
    this.executorService = executorService;
    this.swarmService = swarmService;
    this.egoApiDbDeployer = egoApiDbDeployer;
    this.egoUiDeployer = egoUiDeployer;
  }

  @Override
  public void deploy(@NonNull DmsConfig dmsConfig) {
    swarmService.initializeSwarm();
    val out=  CompletableFuture.runAsync(() -> egoApiDbDeployer.deploy(dmsConfig), executorService)
        .thenRunAsync(() -> egoUiDeployer.deploy(dmsConfig), executorService);
    out.join();
    log.info("sdfdsf");
  }

  @Override
  public void destroy(@NonNull DmsConfig dmsConfig, boolean destroyVolumes) {
    val serviceNames = ComposeServiceResources.stream()
        .map(ComposeServiceResources::toString)
        .collect(toUnmodifiableList());
    swarmService.deleteServices(serviceNames, destroyVolumes);
  }

}
