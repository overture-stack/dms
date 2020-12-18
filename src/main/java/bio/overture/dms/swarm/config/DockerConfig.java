package bio.overture.dms.swarm.config;

import static bio.overture.dms.core.util.Strings.isNotDefined;
import static com.github.dockerjava.core.DefaultDockerClientConfig.createDefaultConfigBuilder;
import static com.github.dockerjava.core.DockerClientImpl.getInstance;

import bio.overture.dms.swarm.properties.DockerProperties;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerConfig {
  private static final String DMS_VOLUME_NAME = "dms-assets";

  private final DockerProperties dockerProperties;

  @Autowired
  public DockerConfig(@NonNull DockerProperties dockerProperties) {
    this.dockerProperties = dockerProperties;
  }

  @Bean
  public DockerClient dockerClient() {
    val config = buildDockerClientConfig();
    return getInstance(config, buildDockerHttpClient(config));
  }

  private DockerClientConfig buildDockerClientConfig() {
    val c = createDefaultConfigBuilder();
    if (isNotDefined(dockerProperties.getHost())) {
      c.withDockerHost(dockerProperties.getHost());
    }
    return c.build();
  }

  private static DockerHttpClient buildDockerHttpClient(DockerClientConfig config) {
    return new ApacheDockerHttpClient.Builder()
        .dockerHost(config.getDockerHost())
        .sslConfig(config.getSSLConfig())
        .build();
  }
}
