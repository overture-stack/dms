package bio.overture.dms.config;

import static com.github.dockerjava.core.DefaultDockerClientConfig.createDefaultConfigBuilder;
import static com.github.dockerjava.core.DockerClientImpl.getInstance;
import static java.util.concurrent.Executors.newFixedThreadPool;

import bio.overture.dms.docker.DockerService;
import bio.overture.dms.util.SpringExecutorService;
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
  private static final String DMS_VOLUME_NAME = "dms-assets";

  @Bean
  public ExecutorService executor() {
    val numThreads = Runtime.getRuntime().availableProcessors();
    return new SpringExecutorService(newFixedThreadPool(numThreads), 4);
  }

  @Bean
  @Autowired
  public DockerService dockerService(DockerClient dockerClient) {
    return new DockerService(dockerClient);
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
