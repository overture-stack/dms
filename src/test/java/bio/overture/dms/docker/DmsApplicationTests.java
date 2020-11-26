package bio.overture.dms.docker;

import static bio.overture.dms.util.FileUtils.readResourcePath;
import static com.github.dockerjava.api.model.MountType.BIND;

import bio.overture.dms.compose.ComposeGraphGenerator;
import bio.overture.dms.compose.ComposeManager;
import bio.overture.dms.compose.ComposeReader;
import bio.overture.dms.compose.ComposeTemplateEngine;
import bio.overture.dms.config.SerializationConfig;
import bio.overture.dms.model.spec.DmsSpec;
import bio.overture.dms.model.spec.EgoSpec;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.velocity.app.VelocityEngine;
import org.javers.core.JaversBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class DmsApplicationTests {

  @Autowired private DockerService dockerService;

  @Autowired private DockerClient dockerClient;

  @Disabled
  @Test
  @SneakyThrows
  public void testDC() {
    val imageName = "docker/compose:alpine-1.27.4";
    dockerClient.pullImageCmd(imageName).start().awaitCompletion();
    //    val dockerComposePath = "src/main/resources/templates/docker-compose.yaml";
    val dockerComposePath = "/templates/docker-compose.yaml";
    val containerName = "docker-compose-cnt";
    dockerService
        .findContainerId(containerName)
        .ifPresent(id -> dockerService.deleteContainer(id, true, false));
    val container =
        dockerClient
            .createContainerCmd(imageName)
            .withName("docker-compose-cnt")
            .withCmd("-p", "dms", "-f", "/docker-compose.yaml", "up", "-d")
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withMounts(
                        List.of(
                            new Mount()
                                .withSource("/var/run/docker.sock")
                                .withType(BIND)
                                .withTarget("/var/run/docker.sock")
                                .withReadOnly(true),
                            new Mount()
                                .withSource("/usr/bin/docker")
                                .withType(BIND)
                                .withTarget("/usr/bin/docker")
                                .withReadOnly(true))))
            .exec();
    val file = readResourcePath(dockerComposePath);
    dockerClient
        .copyArchiveToContainerCmd(container.getId())
        .withHostResource(file.getFile().getAbsolutePath())
        .withRemotePath("/")
        .exec();
    dockerClient.startContainerCmd(container.getId()).exec();
    dockerClient.waitContainerCmd(container.getId());
    dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
    log.info("sdf");
  }

  @Disabled
  @Test
  @SneakyThrows
  public void testParseDockerCompose() {
    val dockerComposePath = "/templates/docker-compose.yaml";
    val volumeName = "robvolume";
    val networkName = "robnetwork";
    val file = readResourcePath(dockerComposePath).getFile();
    val yamlProcessor = new SerializationConfig().yamlSerializer();
    val dcReader = new ComposeReader(yamlProcessor);

    val dc = dcReader.readDockerCompose(file);

    val executor = Executors.newFixedThreadPool(4);
    val generator = new ComposeGraphGenerator(networkName, volumeName, dockerService);
    val dockerComposer = new ComposeManager(executor, generator, dockerService);
    dockerComposer.deploy(dc);

    Thread.sleep(4000);
    val out = dockerComposer.inspectService("ego-server");

    val javers = JaversBuilder.javers().build();
    val diff =
        javers.compare(
            dc.getServices().stream()
                .filter(x -> x.getServiceName().equals("ego-server"))
                .findFirst()
                .get(),
            out.get());

    dockerComposer.destroy(dc, true, false);

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);

    log.info("sdf");
  }

  @Test
  void contextLoads() {}

  @Autowired private VelocityEngine velocityEngine;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestEgoDB {
    private String password;
    private Integer port;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestEgoServer {
    private Integer port;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestData {
    private TestEgoDB egodb;
    private TestEgoServer egoserver;
  }

  @Autowired ComposeTemplateEngine composeTemplateEngine;

  @Test
  @Disabled
  public void testTemplating() {

    val dmsSpec =
        DmsSpec.builder()
            .version("1.2.0")
            .ego(
                EgoSpec.builder()
                    .host("ego.staging.overture.bio")
                    .refreshTokenDurationMS(11111111)
                    .jwtDurationMS(2222222)
                    .apiTokenDurationDays(3333333)
                    .databasePassword("robivolidisk")
                    .sso(
                        EgoSpec.SSOSpec.builder()
                            .facebook(
                                EgoSpec.SSOClientSpec.builder()
                                    .clientId("someFbClientId")
                                    .clientSecret("someFbClientSecret")
                                    .preEstablishedRedirectUri(
                                        "https://ego.staging.overture.bio/something")
                                    .build())
                            .build())
                    .build())
            .build();

    val out = composeTemplateEngine.render(dmsSpec);

    log.info("Sdfsd");
  }
}
