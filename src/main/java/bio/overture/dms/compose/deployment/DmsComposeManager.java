package bio.overture.dms.compose.deployment;

import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_UI;
import static bio.overture.dms.compose.model.ComposeServiceResources.SONG_DB;
import static java.util.stream.Collectors.toUnmodifiableList;

import bio.overture.dms.compose.deployment.ego.EgoApiDbDeployer;
import bio.overture.dms.compose.deployment.song.SongApiDeployer;
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
  private final SongApiDeployer songApiDeployer;

  @Autowired
  public DmsComposeManager(
      @NonNull ExecutorService executorService,
      @NonNull SwarmService swarmService,
      @NonNull EgoApiDbDeployer egoApiDbDeployer,
      @NonNull ServiceDeployer serviceDeployer,
      @NonNull SongApiDeployer songApiDeployer) {
    this.executorService = executorService;
    this.swarmService = swarmService;
    this.egoApiDbDeployer = egoApiDbDeployer;
    this.serviceDeployer = serviceDeployer;
    this.songApiDeployer = songApiDeployer;
  }

  @Override
  public void deploy(@NonNull DmsConfig dmsConfig) {
    swarmService.initializeSwarm();
    swarmService.getOrCreateNetwork(dmsConfig.getNetwork());

    // TODO: there should be 2 modes of deployment. a) makes the most sense and is used here.
    // a) Blocking the deployment of a dependency until its parents is READY
    // b) Not blocking any dependency, and deploy all. Once deployed, run a separate command to poll
    // and check status.
    //    If something didnt start or is not ready, it should report it
    // TODO: this is confusing. May want to use GraphBuilder and ConcurrentGraphTraversal link all
    // the completable futures

    val egoFuture =
        CompletableFuture.runAsync(() -> egoApiDbDeployer.deploy(dmsConfig), executorService)
            .thenRunAsync(getDeployRunnable(dmsConfig, EGO_UI), executorService);

    val songDbFuture =
        CompletableFuture.runAsync(getDeployRunnable(dmsConfig, SONG_DB), executorService);

    // Song API can only deploy once EgoApi and SongDb are BOTH healthy
    val songApiFuture =
        songDbFuture.runAfterBothAsync(
            egoFuture, () -> songApiDeployer.deploy(dmsConfig), executorService);

    // Wait for all futures to complete
    CompletableFuture.allOf(egoFuture, songDbFuture, songApiFuture).join();
  }

  private Runnable getDeployRunnable(
      DmsConfig dmsConfig, ComposeServiceResources composeServiceResource) {
    return () -> serviceDeployer.deploy(dmsConfig, composeServiceResource, true);
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
