package bio.overture.dms.infra.docker;

import bio.overture.dms.infra.graph.GraphBuilder;
import bio.overture.dms.infra.graph.MemoryGraph;
import bio.overture.dms.infra.graph.Node;
import bio.overture.dms.infra.job.DeployJob;
import bio.overture.dms.infra.model.DCService;
import bio.overture.dms.infra.model.DockerCompose;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;

import static java.util.stream.Collectors.toUnmodifiableMap;

@Slf4j
@RequiredArgsConstructor
public class DCGraphGenerator {

  @NonNull private final String networkName;
  @NonNull private final String assetVolumeName;
  @NonNull private final DockerService dockerService;

  public MemoryGraph<DeployJob> generateGraph(@NonNull DockerCompose dc){

    // Ensure docker network and volumes exist
    provisionDockerPrerequisites(networkName, assetVolumeName);

    // Create graph builder
    val gb = GraphBuilder.<DeployJob>builder();
    // Create memoization index for job ctx
    val nodeIndex = dc.getServices().stream()
        .collect(toUnmodifiableMap(DCService::getServiceName, x -> processService(x, gb)));

    // Create dependency edges
    dc.getServices().forEach(childService -> processDeps(childService, gb, nodeIndex));

    return gb.build();
  }

  private void provisionDockerPrerequisites(String networkName, String assetVolumeName){
    // Create asset volume if it does not already exist
    dockerService.createVolume(assetVolumeName);

    // Create network if it does not already exist
    dockerService.getOrCreateNetwork(networkName);
  }

  private DCServiceJobContext processService(@NonNull DCService s, GraphBuilder<DeployJob> gb) {
    val imagePullDeployJob = getOrCreateImagePullJob(s, gb);
    val containerDeployJob = getOrCreateContainerDeployJob(s, gb);
    gb.addEdge(imagePullDeployJob, containerDeployJob);
    return DCServiceJobContext.builder()
        .imagePull(imagePullDeployJob)
        .containerDeploy(containerDeployJob)
        .build();
  }

  private void processDeps(DCService childService, GraphBuilder<DeployJob> gb,
      Map<String, DCServiceJobContext> nodeIndex) {
    childService.getDependsOn().forEach(parentServiceName -> {
      val parentJob = nodeIndex.get(parentServiceName).getContainerDeploy();
      val childJob = nodeIndex.get(childService.getServiceName()).getContainerDeploy();
      gb.addEdge(parentJob.getName(), childJob.getName());
    });
  }

  private DeployJob getOrCreateContainerDeployJob(DCService s, GraphBuilder<DeployJob> gb) {
    val jobName = "deploy:" + s.getServiceName();
    return gb.findNodeByName(jobName)
        .map(Node::getData)
        .orElseGet(() ->
            DeployJob.builder()
                .name(jobName)
                .deployTask(() -> deployContainer(s))
                .build()
        );
  }

  //TODO: refactor
  private void deployContainer(DCService s) {
    dockerService.ping();
    val result = dockerService.findContainerId(s.getServiceName());
    String containerId;
    if (result.isPresent()) {
      containerId = result.get();
      if (!dockerService.isContainerRunning(containerId)) {
        log.info("Deleting container '{}' for id: {}",
            s.getServiceName(), containerId);
        dockerService.deleteContainer(containerId, false, false);
      } else {
        log.info("Container '{}' already running with id: {}",
            s.getServiceName(), containerId);
        return;
      }
    }

    containerId = dockerService.createContainer(networkName, assetVolumeName, s);
    log.info("Created container '{}' in network '{}' with id: {}",
        s.getServiceName(), networkName, containerId);
    dockerService.startContainer(containerId);
    log.info("Started container '{}' in network '{}' with id: {}",
        s.getServiceName(), networkName, containerId);
  }

  private DeployJob getOrCreateImagePullJob(DCService s, GraphBuilder<DeployJob> gb) {
    val imageName = s.getImage();
    val jobName = "image-pull:" + imageName;
    return gb.findNodeByName(jobName)
        .map(Node::getData)
        .orElseGet(() ->
            DeployJob.builder()
                .name(jobName)
                .deployTask(() -> {
                  log.info("Pulling image: {}", imageName);
                  dockerService.pullImage(imageName);
                })
                .build()

        );
  }
}
