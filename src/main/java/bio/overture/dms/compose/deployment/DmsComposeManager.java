package bio.overture.dms.compose.deployment;

import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_UI;
import static bio.overture.dms.compose.model.ComposeServiceResources.MINIO_API;
import static bio.overture.dms.compose.model.ComposeServiceResources.SONG_DB;
import static bio.overture.dms.core.util.Concurrency.waitForCompletableFutures;
import static java.util.stream.Collectors.toUnmodifiableList;

import bio.overture.dms.compose.deployment.ego.EgoApiDbDeployer;
import bio.overture.dms.compose.deployment.score.ScoreApiDeployer;
import static bio.overture.dms.compose.model.ComposeServiceResources.*;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.toUnmodifiableList;

import bio.overture.dms.compose.deployment.ego.EgoApiDbDeployer;
import bio.overture.dms.compose.deployment.elasticsearch.ElasticsearchDeployer;
import bio.overture.dms.compose.deployment.song.SongApiDeployer;
import bio.overture.dms.compose.model.ComposeServiceResources;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.swarm.service.SwarmService;
import java.util.ArrayList;
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
  private final ScoreApiDeployer scoreApiDeployer;
  private final ElasticsearchDeployer elasticsearchDeployer;

  @Autowired
  public DmsComposeManager(
      @NonNull ExecutorService executorService,
      @NonNull SwarmService swarmService,
      @NonNull EgoApiDbDeployer egoApiDbDeployer,
      @NonNull ServiceDeployer serviceDeployer,
      @NonNull SongApiDeployer songApiDeployer,
      @NonNull ScoreApiDeployer scoreApiDeployer,
      @NonNull ElasticsearchDeployer elasticsearchDeployer) {
    this.executorService = executorService;
    this.swarmService = swarmService;
    this.egoApiDbDeployer = egoApiDbDeployer;
    this.serviceDeployer = serviceDeployer;
    this.songApiDeployer = songApiDeployer;
    this.scoreApiDeployer = scoreApiDeployer;
    this.elasticsearchDeployer = elasticsearchDeployer;
  }

  @Override
  public void deploy(@NonNull DmsConfig dmsConfig) {
    swarmService.initializeSwarm();
    swarmService.getOrCreateNetwork(dmsConfig.getNetwork());
    val completableFutures = new ArrayList<CompletableFuture<?>>();

    // TODO: there should be 2 modes of deployment. a) makes the most sense and is used here.
    // a) Blocking the deployment of a dependency until its parents is READY
    // b) Not blocking any dependency, and deploy all. Once deployed, run a separate command to poll
    // and check status.
    //    If something didnt start or is not ready, it should report it
    // TODO: this is confusing. May want to use GraphBuilder and ConcurrentGraphTraversal link all
    // the completable futures

    val egoFuture =
        runAsync(() -> egoApiDbDeployer.deploy(dmsConfig), executorService)
            .thenRunAsync(getDeployRunnable(dmsConfig, EGO_UI), executorService);
    completableFutures.add(egoFuture);

    val songDbFuture =
        CompletableFuture.runAsync(getDeployRunnable(dmsConfig, SONG_DB), executorService);
    completableFutures.add(songDbFuture);

    // Song API can only deploy once EgoApi and SongDb are BOTH healthy
    val songApiFuture =
        songDbFuture.runAfterBothAsync(
            egoFuture, () -> songApiDeployer.deploy(dmsConfig), executorService);
    completableFutures.add(songApiFuture);

    if (dmsConfig.getScore().getS3().isUseMinio()) {
      val minioFuture =
          CompletableFuture.runAsync(getDeployRunnable(dmsConfig, MINIO_API), executorService);
      completableFutures.add(minioFuture);
    }

    // Score API can only deploy once EgoApi is healthy
    val scoreApiFuture =
        egoFuture.thenRunAsync(() -> scoreApiDeployer.deploy(dmsConfig), executorService);
    completableFutures.add(scoreApiFuture);

    val elasticsearchFuture = runAsync(() -> elasticsearchDeployer.deploy(dmsConfig), executorService);
    completableFutures.add(elasticsearchFuture);

    // Wait for all completable futures to complete
    waitForCompletableFutures(completableFutures);
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
