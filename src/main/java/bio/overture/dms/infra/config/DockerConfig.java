package bio.overture.dms.infra.config;

import static com.github.dockerjava.core.DefaultDockerClientConfig.createDefaultConfigBuilder;
import static com.github.dockerjava.core.DockerClientImpl.getInstance;
import static java.util.concurrent.Executors.newFixedThreadPool;

import bio.overture.dms.infra.converter.EgoContainerConverter;
import bio.overture.dms.infra.docker.DockerService;
import bio.overture.dms.infra.env.EnvProcessor;
import bio.overture.dms.infra.service.DeploymentService;
import bio.overture.dms.infra.service.DmsDeploymentService;
import bio.overture.dms.infra.service.DockerComposeClient;
import bio.overture.dms.infra.util.SpringExecutorService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import java.util.concurrent.ExecutorService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerConfig {
  private static final String DOCKER_COMPOSE_TAG = "alpine-1.27.4";
  private static final String DMS_VOLUME_NAME = "dms-assets";

  @Bean
  public ExecutorService executor() {
    val numThreads = Runtime.getRuntime().availableProcessors();
    return new SpringExecutorService(newFixedThreadPool(numThreads), 4);
  }

  @Bean
  public DockerComposeClient dockerComposeService(DockerClient dockerClient) {
    val imageName = "docker/compose:" + DOCKER_COMPOSE_TAG;
    return DockerComposeClient.builder()
        .dockerComposeContainerName("docker-compose-instance")
        .dockerComposeImageName(imageName)
        .projectName("dms")
        .dockerClient(dockerClient)
        .build();
  }

  @Bean
  @Autowired
  public DockerService dockerService(
      DockerClient dockerClient, @Autowired EnvProcessor envProcessor) {
    return new DockerService(DMS_VOLUME_NAME, dockerClient, envProcessor);
  }

  @Bean
  @Autowired
  public DmsDeploymentService dmsDeploymentService(
      ExecutorService executorService,
      DeploymentService deploymentService,
      DockerService dockerService) {
    val egoContainerConverter = new EgoContainerConverter("myRobNetwork");
    return new DmsDeploymentService(
        executorService, egoContainerConverter, deploymentService, dockerService);
  }

  @Bean
  @Autowired
  public DeploymentService deploymentService(
      DockerService dockerService, ExecutorService executorService) {
    return DeploymentService.createDeploymentService(dockerService, executorService);
  }

  @Bean
  public DockerClient dockerClient() {
    val config = buildDockerClientConfig();
    return getInstance(config, buildDockerHttpClient(config));
  }

  private static DockerHttpClient buildDockerHttpClient(DockerClientConfig config) {
    return new ApacheDockerHttpClient.Builder()
        .dockerHost(config.getDockerHost())
        .sslConfig(config.getSSLConfig())
        .build();
  }

  private static DockerClientConfig buildDockerClientConfig() {
    return createDefaultConfigBuilder().build();
  }
}
