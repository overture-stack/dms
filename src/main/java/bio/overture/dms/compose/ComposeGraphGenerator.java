package bio.overture.dms.compose;

import static java.util.stream.Collectors.toUnmodifiableMap;

import bio.overture.dms.docker.DockerService;
import bio.overture.dms.graph.GraphBuilder;
import bio.overture.dms.graph.MemoryGraph;
import bio.overture.dms.graph.Node;
import bio.overture.dms.model.compose.Compose;
import bio.overture.dms.model.compose.ComposeService;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor
@Deprecated
public class ComposeGraphGenerator {

  @NonNull private final String networkName;
  @NonNull private final String assetVolumeName;
  @NonNull private final DockerService dockerService;

  public MemoryGraph<ComposeJob> generateGraph(@NonNull Compose dc) {

    // Ensure docker network and volumes exist
    provisionDockerPrerequisites(networkName, assetVolumeName);

    // Create graph builder
    val gb = GraphBuilder.<ComposeJob>builder();
    // Create memoization index for job ctx
    val nodeIndex =
        dc.getServices().stream()
            .collect(toUnmodifiableMap(ComposeService::getServiceName, x -> processService(x, gb)));

    // Create dependency edges
    dc.getServices().forEach(childService -> processDeps(childService, gb, nodeIndex));

    return gb.build();
  }

  private void provisionDockerPrerequisites(String networkName, String assetVolumeName) {
    // Create asset volume if it does not already exist
    dockerService.createVolume(assetVolumeName);

    // Create network if it does not already exist
    dockerService.getOrCreateNetwork(networkName);
  }

  private ComposeJobContext processService(@NonNull ComposeService s, GraphBuilder<ComposeJob> gb) {
    val imagePullDeployJob = getOrCreateImagePullJob(s, gb);
    val containerDeployJob = getOrCreateContainerDeployJob(s, gb);
    gb.addEdge(imagePullDeployJob, containerDeployJob);
    return ComposeJobContext.builder()
        .imagePull(imagePullDeployJob)
        .containerDeploy(containerDeployJob)
        .build();
  }

  private void processDeps(
      ComposeService childService,
      GraphBuilder<ComposeJob> gb,
      Map<String, ComposeJobContext> nodeIndex) {
    childService
        .getDependsOn()
        .forEach(
            parentServiceName -> {
              val parentJob = nodeIndex.get(parentServiceName).getContainerDeploy();
              val childJob = nodeIndex.get(childService.getServiceName()).getContainerDeploy();
              gb.addEdge(parentJob.getName(), childJob.getName());
            });
  }

  private ComposeJob getOrCreateContainerDeployJob(ComposeService s, GraphBuilder<ComposeJob> gb) {
    val jobName = "deploy:" + s.getServiceName();
    return gb.findNodeByName(jobName)
        .map(Node::getData)
        .orElseGet(
            () -> ComposeJob.builder().name(jobName).deployTask(() -> deployContainer(s)).build());
  }

  // TODO: refactor
  private void deployContainer(ComposeService s) {
    dockerService.ping();
    val result = dockerService.findContainerId(s.getServiceName());
    String containerId;
    if (result.isPresent()) {
      containerId = result.get();
      if (!dockerService.isContainerRunning(containerId)) {
        log.info("Deleting container '{}' for id: {}", s.getServiceName(), containerId);
        dockerService.deleteContainer(containerId, false, false);
      } else {
        log.info("Container '{}' already running with id: {}", s.getServiceName(), containerId);
        return;
      }
    }

    containerId = dockerService.createContainer(networkName, assetVolumeName, s);
    log.info(
        "Created container '{}' in network '{}' with id: {}",
        s.getServiceName(),
        networkName,
        containerId);
    dockerService.startContainer(containerId);
    log.info(
        "Started container '{}' in network '{}' with id: {}",
        s.getServiceName(),
        networkName,
        containerId);
  }

  private ComposeJob getOrCreateImagePullJob(ComposeService s, GraphBuilder<ComposeJob> gb) {
    val imageName = s.getImage();
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
                          dockerService.pullImage(imageName);
                        })
                    .build());
  }
}
