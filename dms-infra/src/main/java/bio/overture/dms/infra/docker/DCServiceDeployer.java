package bio.overture.dms.infra.docker;

import bio.overture.dms.infra.graph.Graph;
import bio.overture.dms.infra.graph.GraphBuilder;
import bio.overture.dms.infra.graph.Node;
import bio.overture.dms.infra.job.DeployJob;
import bio.overture.dms.infra.job.DeployJobCallback;
import bio.overture.dms.infra.model.DCService;
import bio.overture.dms.infra.model.DockerCompose;
import bio.overture.dms.infra.util.Concurrency;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static bio.overture.dms.infra.util.Concurrency.trySubmit;
import static java.util.stream.Collectors.toUnmodifiableMap;

@Slf4j
@RequiredArgsConstructor
public class DCServiceDeployer {

  @NonNull private final String volumeName;
  @NonNull private final String networkName;
  @NonNull private final ExecutorService executorService;
  @NonNull private final DockerService dockerService;

  public void deployDC(DockerCompose dc) {
    // Init
    val assetVolumeName = dockerService.createVolumeWithAssets();

    // Create network if it does not already exist
    val network = dockerService.getNetwork(networkName);

    // Create memoization index for job ctx
    val nodeIndex = dc.getServices().stream()
        .collect(toUnmodifiableMap(DCService::getServiceName, x -> processService(networkName, assetVolumeName, x)));

    // Create graph builder
    val gb = Graph.<DeployJob>builder();

    // Create ImagePull -> ContainerDeploy edge (i.e image pull before container deploy)
    nodeIndex.values().forEach(e -> gb.addEdge(e.getImagePull(), e.getContainerDeploy()));

    // Create dependency edges
    dc.getServices().forEach(childService -> processDeps(childService, gb, nodeIndex));

    val graph = gb.build();
    val deployJobCallback = new DeployJobCallback(executorService, graph);
    deployJobCallback.run();
  }

  @SneakyThrows
  public void destroy(DockerCompose dc) {
   val latch = new CountDownLatch(dc.getServices().size()) ;
   dc.getServices().forEach(s -> asyncDeleteService(s,latch));
   latch.await(1, TimeUnit.HOURS);
  }

  private void asyncDeleteService(DCService s, CountDownLatch latch){
    trySubmit(executorService, () -> {
      syncDeleteService(s);
      latch.countDown();
    });
  }

  private void syncDeleteService(DCService s){
    dockerService.findContainerId(s.getServiceName())
        .ifPresent(id -> {
          log.info("Deleting container '{}' forcefully: {}", s.getServiceName(), id);
          dockerService.deleteContainer(id, true, false);
        });
  }

  private void processDeps(DCService childService, GraphBuilder<DeployJob> gb,
      Map<String, DCServiceJobContext> nodeIndex) {
    childService.getDependsOn().forEach(parentServiceName -> {
      val parentJob = nodeIndex.get(parentServiceName).getContainerDeploy();
      val childJob = nodeIndex.get(childService.getServiceName()).getContainerDeploy();
      gb.addEdge(parentJob, childJob);
    });
  }

   TODO need to prevent the creation of new Nodes for the same task. This is why its not working for ego-db and ego-db1
  private DCServiceJobContext processService(@NonNull String networkName, @NonNull String assetVolumeName,
      @NonNull DCService s) {
    val imagePullNode = Node.of(createImagePullJob(s));
    val containerDeployNode = Node.of(createContainerDeployJob(networkName, s, assetVolumeName));
    return DCServiceJobContext.builder()
        .imagePull(imagePullNode)
        .containerDeploy(containerDeployNode)
        .build();
  }

  private DeployJob createContainerDeployJob(String networkName, DCService s, String assetVolumeName) {
    return DeployJob.builder()
        .name("deploy:" + s.getServiceName())
        .deployTask(() -> deployContainer(networkName, s, assetVolumeName))
        .build();
  }

  //TODO: refactor
  private void deployContainer(String networkName, DCService s, String assetVolumeName) {
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
      }
    }

    containerId = dockerService.createContainer(networkName, assetVolumeName, s);
    log.info("Created container '{}' in network '{}' with id: {}",
        s.getServiceName(), this.networkName, containerId);
    dockerService.startContainer(containerId);
    log.info("Started container '{}' in network '{}' with id: {}",
        s.getServiceName(), this.networkName, containerId);
  }

  private DeployJob createImagePullJob(DCService s) {
    val imageName = s.getImage();
    return DeployJob.builder()
        .name("image-pull:" + imageName)
        .deployTask(() -> {
          log.info("Pulling image: {}", imageName);
          dockerService.pullImage(imageName);
        })
        .build();
  }

}
