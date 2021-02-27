package bio.overture.dms.compose.deployment;

import static bio.overture.dms.compose.model.ComposeServiceResources.*;
import static bio.overture.dms.core.util.Concurrency.waitForCompletableFutures;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.toUnmodifiableList;

import bio.overture.dms.compose.deployment.ego.EgoApiDbDeployer;
import bio.overture.dms.compose.deployment.elasticsearch.ElasticsearchDeployer;
import bio.overture.dms.compose.deployment.score.ScoreApiDeployer;
import bio.overture.dms.compose.deployment.song.SongApiDeployer;
import bio.overture.dms.compose.model.ComposeServiceResources;
import bio.overture.dms.core.Messenger;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.MaestroConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.swarm.properties.DockerProperties;
import bio.overture.dms.swarm.service.SwarmService;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.NotImplementedException;
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
  private final Messenger messenger;
  private final boolean dmsRunningInDocker;

  @Autowired
  public DmsComposeManager(
      @NonNull ExecutorService executorService,
      @NonNull SwarmService swarmService,
      @NonNull EgoApiDbDeployer egoApiDbDeployer,
      @NonNull ServiceDeployer serviceDeployer,
      @NonNull SongApiDeployer songApiDeployer,
      @NonNull ScoreApiDeployer scoreApiDeployer,
      @NonNull ElasticsearchDeployer elasticsearchDeployer,
      @NonNull DockerProperties dockerProperties,
      @NonNull Messenger messenger) {
    this.executorService = executorService;
    this.swarmService = swarmService;
    this.egoApiDbDeployer = egoApiDbDeployer;
    this.serviceDeployer = serviceDeployer;
    this.songApiDeployer = songApiDeployer;
    this.scoreApiDeployer = scoreApiDeployer;
    this.elasticsearchDeployer = elasticsearchDeployer;
    this.dmsRunningInDocker = dockerProperties.getRunAs();
    this.messenger = messenger;
  }

  @Override
  public void deploy(@NonNull DmsConfig dmsConfig) {
    swarmService.initializeSwarm();
    swarmService.getOrCreateNetwork(dmsConfig.getNetwork());
    val completableFutures = new ArrayList<CompletableFuture<?>>();

    val egoFuture =
        runAsync(() -> egoApiDbDeployer.deploy(dmsConfig), executorService)
            .thenRunAsync(getDeployRunnable(dmsConfig, EGO_UI, messenger), executorService);
    completableFutures.add(egoFuture);

    val songDbFuture =
        CompletableFuture.runAsync(
            getDeployRunnable(dmsConfig, SONG_DB, messenger), executorService);
    completableFutures.add(songDbFuture);

    // Song API can only deploy once EgoApi and SongDb are BOTH healthy
    val songApiFuture =
        songDbFuture.runAfterBothAsync(
            egoFuture, () -> songApiDeployer.deploy(dmsConfig), executorService);
    completableFutures.add(songApiFuture);

    if (dmsConfig.getScore().getS3().isUseMinio()) {
      val minioFuture =
          CompletableFuture.runAsync(
              getDeployRunnable(dmsConfig, MINIO_API, messenger), executorService);
      completableFutures.add(minioFuture);
    }

    // Score API can only deploy once EgoApi is healthy
    val scoreApiFuture =
        egoFuture.thenRunAsync(() -> scoreApiDeployer.deploy(dmsConfig), executorService);
    completableFutures.add(scoreApiFuture);

    val elasticMaestroFuture =
        runAsync(() -> elasticsearchDeployer.deploy(dmsRunningInDocker, dmsConfig), executorService)
            .thenRunAsync(
                getMaestroDeployRunnable(dmsConfig, dmsRunningInDocker, messenger),
                executorService);
    completableFutures.add(elasticMaestroFuture);

    val arrangerFuture =
        elasticMaestroFuture
            .thenRunAsync(getDeployRunnable(dmsConfig, ARRANGER_SERVER, messenger), executorService)
            .thenRunAsync(getDeployRunnable(dmsConfig, ARRANGER_UI, messenger), executorService);
    completableFutures.add(arrangerFuture);

    val dmsUIFuture =
        arrangerFuture.thenRunAsync(
            getDeployRunnable(dmsConfig, DMS_UI, messenger), executorService);
    completableFutures.add(dmsUIFuture);

    CountDownLatch latch = new CountDownLatch(1);
    CompletableFuture.runAsync(
        () -> {
          delay(5 * 1000);
          while (latch.getCount() == 1) {
            messenger.send("Still Working...");
            delay(15 * 1000);
          }
        });

    try {
      // Wait for all completable futures to complete
      waitForCompletableFutures(completableFutures);
    } catch (Exception e) {
      messenger.send("Failed to deploy services");
      latch.countDown();
      throw e;
    } finally {
      if (latch.getCount() == 1) latch.countDown();
    }
  }

  private Runnable getMaestroDeployRunnable(
      DmsConfig dmsConfig, Boolean dmsRunningInDocker, Messenger messenger) {
    return () -> {
      serviceDeployer.deploy(dmsConfig, MAESTRO, true);
      val host =
          DmsComposeManager.resolveServiceHost(
              dmsConfig.getClusterRunMode(),
              dmsConfig.getMaestro().getHostPort());
      try {
        ServiceDeployer.waitForOk(
            "http://" + host,
            dmsConfig.getHealthCheck().getRetries(),
            dmsConfig.getHealthCheck().getDelaySec());
      } catch (Exception e) {
        messenger.send("❌ Health check failed for Maestro");
        throw e;
      }
      messenger.send(
          "\uD83C\uDFC1️ Deployment for service %s finished successfully", MAESTRO.toString());
    };
  }

  private Runnable getDeployRunnable(
      DmsConfig dmsConfig, ComposeServiceResources composeServiceResource, Messenger messenger) {
    return () -> {
      serviceDeployer.deploy(dmsConfig, composeServiceResource, true);
      messenger.send(
          "\uD83C\uDFC1️ Deployment for service %s finished successfully",
          composeServiceResource.toString());
    };
  }

  public static String resolveServiceHost(
      ClusterRunModes runModes,
      int hostPort) {
    if (runModes == ClusterRunModes.PRODUCTION) {
      throw new NotImplementedException("");
    }
    if (runModes == ClusterRunModes.LOCAL) {
      return ("localhost:" + hostPort);
    } else {
      throw new RuntimeException("invalid cluster mode");
    }
  }

  public static void delay(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
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
