package bio.overture.dms.compose.docker;

import static bio.overture.dms.core.util.FileUtils.readResourcePath;
import static com.github.dockerjava.api.model.MountType.BIND;

import bio.overture.dms.compose.service.ComposeStackGraphGenerator;
import bio.overture.dms.compose.service.ComposeStackManager;
import bio.overture.dms.compose.service.ComposeStackRenderEngine;
import bio.overture.dms.compose.service.SwarmService;
import bio.overture.dms.compose.service.SwarmSpecService;
import bio.overture.dms.core.model.spec.DmsSpec;
import bio.overture.dms.core.model.spec.EgoSpec;
import bio.overture.dms.core.util.ObjectSerializer;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.ContainerSpec;
import com.github.dockerjava.api.model.Driver;
import com.github.dockerjava.api.model.EndpointResolutionMode;
import com.github.dockerjava.api.model.EndpointSpec;
import com.github.dockerjava.api.model.HealthCheck;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.NetworkAttachmentConfig;
import com.github.dockerjava.api.model.PortConfig;
import com.github.dockerjava.api.model.PortConfigProtocol;
import com.github.dockerjava.api.model.ResourceRequirements;
import com.github.dockerjava.api.model.ResourceSpecs;
import com.github.dockerjava.api.model.Service;
import com.github.dockerjava.api.model.ServiceModeConfig;
import com.github.dockerjava.api.model.ServiceReplicatedModeOptions;
import com.github.dockerjava.api.model.ServiceRestartCondition;
import com.github.dockerjava.api.model.ServiceRestartPolicy;
import com.github.dockerjava.api.model.ServiceSpec;
import com.github.dockerjava.api.model.TaskSpec;
import com.github.dockerjava.api.model.UpdateConfig;
import com.github.dockerjava.api.model.UpdateFailureAction;
import com.github.dockerjava.api.model.UpdateOrder;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Disabled
public class DmsApplicationTests {

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

  // NOTE: https://docs.docker.com/engine/swarm/how-swarm-mode-works/swarm-task-states/
  @Autowired ObjectSerializer yamlSerializer;
  @Autowired SwarmSpecService swarmSpecService;

  @Test
  @Disabled
  @SneakyThrows
  public void testSwarm() {

    val imageName = "postgres:11.1";
    val networkName = "rob-network";
    //    dockerClient.removeImageCmd(imageName).withForce(true).withNoPrune(true).exec();
    val swarmService = new SwarmService(dockerClient, swarmSpecService);
    val networkId = dockerService.getOrCreateNetwork("rob-network");

    swarmService.initializeSwarm();
    //    dockerClient.initializeSwarmCmd(swarmSpec).exec();

    val serviceSpec =
        new ServiceSpec()
            .withName("ego-db")
            .withEndpointSpec(
                new EndpointSpec()
                    .withMode(EndpointResolutionMode.VIP)
                    .withPorts(
                        List.of(
                            new PortConfig()
                                .withName("psql")
                                .withProtocol(PortConfigProtocol.TCP)
                                .withPublishMode(PortConfig.PublishMode.ingress)
                                .withTargetPort(5432)
                                .withPublishedPort(8432))))
            .withNetworks(List.of(new NetworkAttachmentConfig().withTarget(networkId)))
            .withUpdateConfig(
                new UpdateConfig()
                    .withFailureAction(UpdateFailureAction.ROLLBACK)
                    .withOrder(UpdateOrder.START_FIRST)
                    .withMaxFailureRatio(0.8f))
            .withRollbackConfig(
                new UpdateConfig()
                    .withFailureAction(UpdateFailureAction.PAUSE)
                    .withOrder(UpdateOrder.START_FIRST)
                    .withMaxFailureRatio(0.8f))
            .withTaskTemplate(
                new TaskSpec()
                    .withLogDriver(new Driver().withName("json-file"))
                    .withNetworks(List.of(new NetworkAttachmentConfig().withTarget(networkId)))
                    .withRestartPolicy(
                        new ServiceRestartPolicy()
                            .withCondition(ServiceRestartCondition.ON_FAILURE)
                            .withDelay(5000000L)
                            .withMaxAttempts(10L))
                    .withContainerSpec(
                        new ContainerSpec()
                            // https://github.com/peter-evans/docker-compose-healthcheck
                            .withImage(imageName)
                            .withEnv(List.of("POSTGRES_DB=ego", "POSTGRES_PASSWORD=password"))
                            .withTty(false)
                            .withOpenStdin(false)
                            .withReadOnly(false)
                            .withStopSignal("SIGINT")
                            .withStopGracePeriod(120000000L)
                            .withHealthCheck(
                                new HealthCheck()
                                    .withTest(List.of("CMD-SHELL", "pg_isready -U postgres"))
                                //                    .withInterval(10*1000000L)
                                //                    .withTimeout(10*1000000L)
                                //                    .withRetries(5)
                                ))
                //            .withResources(new ResourceRequirements()
                //                .withLimits(new ResourceSpecs()
                //                    .withMemoryBytes()
                //                )
                //            )
                );
    val output = this.yamlSerializer.serializeValue(serviceSpec);

    dockerClient.listServicesCmd().exec().stream()
        .filter(x -> x.getSpec().getName().equals(serviceSpec.getName()))
        .map(Service::getId)
        .distinct()
        .forEach(x -> dockerClient.removeServiceCmd(x).exec());

    val serviceId = dockerClient.createServiceCmd(serviceSpec).exec().getId();
    val inspecServiceResp = dockerClient.inspectServiceCmd(serviceId).exec();

    val state =
        swarmService.waitForServiceRunning(serviceSpec.getName(), 10, Duration.ofSeconds(2));

    log.info("sdf");
  }

  @Test
  @Disabled
  public void testSer() {
    ServiceSpec serviceSpec =
        new ServiceSpec()
            .withName("ego-db")
            .withMode(
                new ServiceModeConfig()
                    .withReplicated(new ServiceReplicatedModeOptions().withReplicas(1)))
            .withEndpointSpec(
                new EndpointSpec()
                    .withMode(EndpointResolutionMode.VIP)
                    .withPorts(
                        List.of(
                            new PortConfig()
                                .withName("psql")
                                .withProtocol(PortConfigProtocol.TCP)
                                .withPublishMode(PortConfig.PublishMode.ingress)
                                .withTargetPort(5432)
                                .withPublishedPort(8432))))
            .withNetworks(List.of(new NetworkAttachmentConfig().withTarget("somenetwoekid")))
            .withUpdateConfig(
                new UpdateConfig()
                    .withFailureAction(UpdateFailureAction.ROLLBACK)
                    .withOrder(UpdateOrder.START_FIRST)
                    .withMaxFailureRatio(0.8f))
            .withRollbackConfig(
                new UpdateConfig()
                    .withFailureAction(UpdateFailureAction.PAUSE)
                    .withOrder(UpdateOrder.START_FIRST)
                    .withMaxFailureRatio(0.8f))
            .withTaskTemplate(
                new TaskSpec()
                    .withForceUpdate(0)
                    .withLogDriver(new Driver().withName("json-file"))
                    .withNetworks(
                        List.of(new NetworkAttachmentConfig().withTarget("somenetworkId")))
                    .withRestartPolicy(
                        new ServiceRestartPolicy()
                            .withCondition(ServiceRestartCondition.ON_FAILURE)
                            .withDelay(5000000L)
                            .withWindow(2020202L)
                            .withMaxAttempts(10L))
                    .withContainerSpec(
                        new ContainerSpec()
                            .withCommand(List.of("some", "command"))
                            .withArgs(List.of("some", "arguments", "for", "the command"))
                            .withHostname("some hostname")
                            .withMounts(
                                List.of(
                                    new Mount()
                                        .withType(BIND)
                                        .withSource("/path/to/HostPath")
                                        .withTarget("/path/to/ContainerPath")
                                        .withReadOnly(false)))
                            // https://github.com/peter-evans/docker-compose-healthcheck
                            .withImage("postgres:11.1")
                            .withEnv(List.of("POSTGRES_DB=ego", "POSTGRES_PASSWORD=password"))
                            .withTty(false)
                            .withOpenStdin(false)
                            .withReadOnly(false)
                            .withStopSignal("SIGINT")
                            .withStopGracePeriod(120000000L)
                            .withHealthCheck(
                                new HealthCheck()
                                    .withTest(List.of("CMD-SHELL", "pg_isready -U postgres"))
                                    .withInterval(10 * 1000000L)
                                    .withTimeout(10 * 1000000L)
                                    .withRetries(5)))
                    .withResources(
                        new ResourceRequirements()
                            .withLimits(
                                new ResourceSpecs().withMemoryBytes(2323234L).withNanoCPUs(292929L))
                            .withReservations(
                                new ResourceSpecs()
                                    .withMemoryBytes(2898900909L)
                                    .withNanoCPUs(98988777L))));
    val out = yamlSerializer.serializeValue(serviceSpec);
    log.info("Sdf");
  }

  @Autowired private ComposeStackRenderEngine renderEngine;
  @Autowired private SwarmService swarmService;

  @Test
  @Disabled
  @SneakyThrows
  public void testRrR() {
    val dmsSpec =
        DmsSpec.builder()
            .version("234")
            .ego(
                EgoSpec.builder()
                    .apiHostPort(8080)
                    .apiTokenDurationDays(30)
                    .databasePassword("somePassword2134")
                    .dbHostPort(10432)
                    .host("ego.example.com")
                    .jwtDurationMS(3 * 60 * 60 * 1000)
                    .refreshTokenDurationMS(3 * 3600 * 1000)
                    .sso(EgoSpec.SSOSpec.builder().build())
                    .build())
            .build();
    val d = renderEngine.render(dmsSpec);
    val generator = new ComposeStackGraphGenerator("dms-network", "someVolume", swarmService);
    val executor = Executors.newFixedThreadPool(10);
    val manager = new ComposeStackManager(executor, generator, swarmService);
    manager.deploy(d);

    //    val networkId = swarmService.getOrCreateNetwork("dms-network");
    //
    //    for (val s : d.getServices()){
    //      val result = swarmService.findSwarmService(s.getName());
    //      if (result.isPresent()){
    //        val version = dockerClient.inspectServiceCmd(result.get()).exec().getVersion();
    //        val tasks = dockerClient.listTasksCmd().withServiceFilter(s.getName()).exec().stream()
    //            .filter(x -> x.getServiceId().equals(result.get()))
    //            .collect(Collectors.toUnmodifiableList());
    //        dockerClient.updateServiceCmd(result.get(),
    // s.getServerSpec()).withVersion(version.getIndex()).exec();
    //      } else {
    //        val resp = dockerClient.createServiceCmd(s.getServerSpec()).exec();
    //      }
    //    }

    //    dockerClient.removeServiceCmd(resp.getId()).exec();
    //    dockerClient.removeNetworkCmd(networkId).exec();
    //    dockerClient.removeImageCmd("postgres:11.1").withForce(true).exec();
    log.info("Sdf");
  }
}
