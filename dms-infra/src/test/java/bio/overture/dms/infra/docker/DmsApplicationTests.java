package bio.overture.dms.infra.docker;

import bio.overture.dms.infra.config.JacksonConfig;
import bio.overture.dms.infra.service.DCReader;
import bio.overture.dms.infra.service.DmsDeploymentService;
import bio.overture.dms.infra.service.DockerComposeClient;
import bio.overture.dms.infra.spec.DmsSpec;
import bio.overture.dms.infra.spec.EgoSpec;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static bio.overture.dms.infra.util.FileUtils.readResourcePath;
import static com.github.dockerjava.api.model.MountType.BIND;
import static java.util.stream.Collectors.toUnmodifiableMap;

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
    val volumeName= "robvolume";
    val networkName ="robnetwork";
    val file = readResourcePath(dockerComposePath).getFile();
    val yamlProcessor = new JacksonConfig().yamlProcessor();
    val dcReader = new DCReader(yamlProcessor);

    val dc = dcReader.readDockerCompose(file);

    val executor = Executors.newFixedThreadPool(4);
    val retryTemplate = new RetryTemplate();
    val deployer = new DCServiceDeployer(volumeName, networkName, executor, dockerService, retryTemplate);
    deployer.deployDC(dc);
    deployer.destroy(dc);

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);

    log.info("sdf");
  }

  @Test
  void contextLoads() {}

}
