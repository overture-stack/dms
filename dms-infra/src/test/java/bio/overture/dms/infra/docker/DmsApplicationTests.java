package bio.overture.dms.infra.docker;

import bio.overture.dms.infra.config.JacksonConfig;
import bio.overture.dms.infra.graph.Graph;
import bio.overture.dms.infra.graph.GraphBuilder;
import bio.overture.dms.infra.graph.Node;
import bio.overture.dms.infra.job.DeployJob;
import bio.overture.dms.infra.job.DeployJobCallback;
import bio.overture.dms.infra.model.DCService;
import bio.overture.dms.infra.model.DockerCompose;
import bio.overture.dms.infra.service.DCReader;
import bio.overture.dms.infra.service.DmsDeploymentService;
import bio.overture.dms.infra.service.DockerComposeClient;
import bio.overture.dms.infra.spec.DmsSpec;
import bio.overture.dms.infra.spec.EgoSpec;
import bio.overture.dms.infra.util.FileUtils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static bio.overture.dms.infra.util.FileUtils.readResourcePath;
import static com.github.dockerjava.api.model.MountType.BIND;

@Slf4j
@SpringBootTest
class DmsApplicationTests {

  @Autowired
  private DmsDeploymentService dmsDeploymentService;

  @Autowired
  private DockerService dockerService;

  @Autowired
  private DockerClient dockerClient;

  @Autowired
  private DockerComposeClient dockerComposeClient;

  @Disabled
  @Test
  @SneakyThrows
  public void teste(){
    val egoSpec = EgoSpec.builder()
        .host("https://example.org")
        .build();
    val dmsSpec = DmsSpec.builder()
        .version("1.0.0")
        .ego(egoSpec)
        .build();
    dmsDeploymentService.deploy(dmsSpec);

    log.info("Sdf");
  }

  @Disabled
  @Test
  @SneakyThrows
  public void testDC(){
    val imageName = "docker/compose:alpine-1.27.4";
    dockerClient.pullImageCmd(imageName).start().awaitCompletion();
//    val dockerComposePath = "src/main/resources/templates/docker-compose.yaml";
    val dockerComposePath = "/templates/docker-compose.yaml";
    val containerName = "docker-compose-cnt";
    dockerService.findContainerId(containerName)
        .ifPresent(id -> dockerService.deleteContainer(id,true, false));
    val container = dockerClient.createContainerCmd(imageName)
        .withName("docker-compose-cnt")
        .withCmd("-p",  "dms", "-f", "/docker-compose.yaml", "up", "-d")
        .withHostConfig(HostConfig.newHostConfig().withMounts(
            List.of(
            new Mount().withSource("/var/run/docker.sock").withType(BIND).withTarget("/var/run/docker.sock").withReadOnly(true),
            new Mount().withSource("/usr/bin/docker").withType(BIND).withTarget("/usr/bin/docker").withReadOnly(true))
        ))
        .exec();
    val file = readResourcePath(dockerComposePath);
    dockerClient
        .copyArchiveToContainerCmd(container.getId())
        .withHostResource(file.getFile().getAbsolutePath())
        .withRemotePath("/")
        .exec();
    dockerClient.startContainerCmd(container.getId())
        .exec();
    dockerClient.waitContainerCmd(container.getId());
    dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
    log.info("sdf");
  }

  @Disabled
  @Test
  @SneakyThrows
  public void testROb(){
    val dockerComposePath = "/templates/docker-compose.yaml";
    val file = readResourcePath(dockerComposePath).getFile();
    val logOutput = dockerComposeClient.runCommand(file, "ps");
    val logOutput2 = dockerComposeClient.runCommand(file, "up -d");
    val logOutput4 = dockerComposeClient.runCommand(file, "ps");
    val logOutput23 = dockerComposeClient.runCommand(file, "kill ego-server");
    val logOutput234 = dockerComposeClient.runCommand(file, "up -d ego-server2");
    log.info("sdf");

  }

  @Disabled
  @Test
  @SneakyThrows
  public void testParseDockerCompose(){
    val dockerComposePath = "/templates/docker-compose.yaml";
    val file = readResourcePath(dockerComposePath).getFile();
    val yamlProcessor = new JacksonConfig().yamlProcessor();
    val dcReader = new DCReader(yamlProcessor);

    val dc = dcReader.readDockerCompose(file);


    val executor = Executors.newFixedThreadPool(4);
    val deployer = new DCServiceDeployer("rob-test-123", executor, dockerService);
    deployer.deployDC(dc);

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);

    log.info("sdf");
  }

  @RequiredArgsConstructor
  public static class DCServiceDeployer {

    @NonNull private final String networkName;
    @NonNull private final ExecutorService executorService;
    @NonNull private final DockerService dockerService;

    public void deployDC(DockerCompose dc){

      // Create network if it does not already exist
      val networkId = dockerService.getNetwork(networkName).getId();

      // Create memoization index for job ctx
      val nodeIndex= dc.getServices().stream()
          .collect(Collectors.toUnmodifiableMap(DCService::getServiceName, x -> processService(networkId, x)));

      // Create graph builder
      val gb = Graph.<DeployJob>builder();

      // Create ImagePull -> ContainerDeploy edge (i.e image pull before container deploy)
      nodeIndex.values().forEach(e -> gb.addEdge(e.getImagePull(), e.getContainerDeploy()) );

      // Create dependency edges
      dc.getServices().forEach(childService -> processDeps(childService, gb, nodeIndex));

      val graph = gb.build();
      val deployJobCallback = new DeployJobCallback(executorService, graph);
      deployJobCallback.run();
    }

    private void processDeps(DCService childService, GraphBuilder<DeployJob> gb, Map<String, DCServiceJobContext> nodeIndex){
      childService.getDependsOn().forEach(parentServiceName -> {
        val parentJob = nodeIndex.get(parentServiceName).getContainerDeploy();
        val childJob= nodeIndex.get(childService.getServiceName()).getContainerDeploy();
        gb.addEdge(parentJob, childJob );
      });
    }

    @Value
    @Builder
    public static class DCServiceJobContext {
      @NonNull private final Node<DeployJob> imagePull;
      @NonNull private final Node<DeployJob> containerDeploy;
    }

    private DCServiceJobContext processService(@NonNull String networkId, @NonNull DCService s) {
      val imagePullNode= Node.of(createImagePullJob(s));
      val containerDeployNode= Node.of(createContainerDeployJob(networkId, s));
      return DCServiceJobContext.builder()
          .imagePull(imagePullNode)
          .containerDeploy(containerDeployNode)
          .build();
    }

    private DeployJob createContainerDeployJob(String networkId, DCService s){
      return DeployJob.builder()
          .name("deploy:"+s.getServiceName())
          .deployTask(() -> deployContainer(networkId, s))
          .build();
    }

    private void deployContainer(String networkId, DCService s){
      dockerService.ping();
      val result = dockerService.findContainerId(s.getServiceName());
      String containerId;
      if (result.isPresent()){
        containerId = result.get();
        if (!dockerService.isContainerRunning(containerId)){
          log.info("Deleting container '{}' for id: {}",
              s.getServiceName(), containerId);
          dockerService.deleteContainer(containerId, false, false);
        } else {
          log.info("Container '{}' already running with id: {}",
              s.getServiceName(), containerId);
        }
      }

      containerId = dockerService.createContainer(networkId,s);
      log.info("Created container '{}' in network '{}' with id: {}",
          s.getServiceName(), networkName, containerId );
      dockerService.startContainer(containerId);
      log.info("Started container '{}' in network '{}' with id: {}",
          s.getServiceName(), networkName, containerId );
    }


    private DeployJob createImagePullJob(DCService s){
      val imageName = s.getImage();
      return DeployJob.builder()
          .name("image-pull:"+imageName)
          .deployTask(() -> {
            log.info("Pulling image: {}", imageName);
            dockerService.pullImage(imageName);
          })
          .build();
    }

  }


  @Test
  void contextLoads() {}
}
