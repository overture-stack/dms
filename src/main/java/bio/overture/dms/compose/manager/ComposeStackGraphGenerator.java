package bio.overture.dms.compose.manager;

import static bio.overture.dms.compose.model.stack.DependencyTypes.SERVICE_HEALTHY;
import static bio.overture.dms.compose.model.stack.DependencyTypes.SERVICE_STARTED;
import static bio.overture.dms.core.exception.NotFoundException.buildNotFoundException;
import static java.util.stream.Collectors.toUnmodifiableMap;

import bio.overture.dms.compose.model.job.ComposeJob;
import bio.overture.dms.compose.model.job.ComposeJobContext;
import bio.overture.dms.compose.model.stack.ComposeService;
import bio.overture.dms.compose.model.stack.ComposeStack;
import bio.overture.dms.graph.GraphBuilder;
import bio.overture.dms.graph.MemoryGraph;
import bio.overture.dms.graph.Node;
import bio.overture.dms.swarm.service.SwarmService;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor
public class ComposeStackGraphGenerator {

  @NonNull private final String networkName;
  @NonNull private final SwarmService swarmService;

  public void init() {
    // Ensure docker network exist, otherwise create them
    provisionDockerPrerequisites(networkName);
  }

  public MemoryGraph<ComposeJob> generateGraph(@NonNull ComposeStack cs) {

    // Create graph builder
    val gb = GraphBuilder.<ComposeJob>builder();
    // Create memoization index for job ctx
    val nodeIndex =
        cs.getServices().stream()
            .collect(toUnmodifiableMap(ComposeService::getName, x -> processService(x, gb)));

    // Create dependency edges
    cs.getServices().forEach(childService -> processDeps(childService, gb, nodeIndex));

    return gb.build();
  }

  private void provisionDockerPrerequisites(String networkName) {
    // Create network if it does not already exist
    swarmService.getOrCreateNetwork(networkName);
  }

  private ComposeJobContext processService(@NonNull ComposeService s, GraphBuilder<ComposeJob> gb) {
    // NOTE: do not need to pull the image. swarm automatically does it
    val containerDeployJob = getOrCreateContainerDeployJob(s, gb);
    gb.addNode(containerDeployJob);
    return ComposeJobContext.builder().containerDeploy(containerDeployJob).build();
  }

  private void processDeps(
      ComposeService childService,
      GraphBuilder<ComposeJob> gb,
      Map<String, ComposeJobContext> nodeIndex) {
    childService
        .getDependencies()
        .forEach(
            dependency -> {
              val parentServiceName = dependency.getName();
              val dependencyType = dependency.getType();
              if (dependencyType == SERVICE_STARTED) {
                // TODO: check if parent is running, and wait with a timeout. There should be
                // retries as well
              } else if (dependencyType == SERVICE_HEALTHY) {
                // TODO: check if parent is running, and wait for it to be in a running state, then
                // check via externally configured health check if its ready. There should be a
                // timeout on this whole process, along with retries
                //                val depDeploymentState =
                // swarmService.waitForServiceRunning(dependency.getName(), NUM_RETRIES,
                // RETRY_DURATION);
              } else {
                throw buildNotFoundException("Unknown dependencyType type '%s'", dependencyType);
              }

              val parentJob = nodeIndex.get(parentServiceName).getContainerDeploy();
              val childJob = nodeIndex.get(childService.getName()).getContainerDeploy();
              gb.addEdge(parentJob.getName(), childJob.getName());
            });
  }

  private ComposeJob getOrCreateContainerDeployJob(ComposeService s, GraphBuilder<ComposeJob> gb) {
    val jobName = "deploy:" + s.getName();
    return gb.findNodeByName(jobName)
        .map(Node::getData)
        .orElseGet(
            () -> ComposeJob.builder().name(jobName).deployTask(() -> deployService(s)).build());
  }

  // TODO: refactor
  private void deployService(ComposeService s) {
    swarmService.ping();

    val result = swarmService.findSwarmServiceInfo(s.getName(), true);
    if (result.isPresent()) {
      log.debug("Found service '{}' info, updating existing service spec", s.getName());
      val info = result.get();
      swarmService.updateSwarmService(info.getId(), s.getServerSpec(), info.getVersion());
    } else {
      log.debug("Service '{}' info was NOT found, create new service spec", s.getName());
      swarmService.createSwarmService(s.getServerSpec());
    }
  }

  private ComposeJob getOrCreateImagePullJob(ComposeService s, GraphBuilder<ComposeJob> gb) {
    val imageName = s.getServerSpec().getTaskTemplate().getContainerSpec().getImage();
    val jobName = "image-pull:" + imageName;
    return gb.findNodeByName(jobName)
        .map(Node::getData)
        .orElseGet(
            () ->
                ComposeJob.builder()
                    .name(jobName)
                    .deployTask(
                        () -> {
                          log.info("Pulling image: {}", imageName);
                          swarmService.pullImage(imageName);
                        })
                    .build());
  }
}
