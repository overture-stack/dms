package bio.overture.dms.infra.config;

import bio.overture.dms.infra.converter.EgoSpecConverter;
import bio.overture.dms.infra.docker.DockerService;
import bio.overture.dms.infra.env.EnvProcessor;
import bio.overture.dms.infra.service.EgoDeploymentService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import kong.unirest.apache.ApacheClient;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.dockerjava.core.DefaultDockerClientConfig.createDefaultConfigBuilder;
import static com.github.dockerjava.core.DockerClientImpl.getInstance;

@Configuration
public class DockerConfig {

  @Bean
  public DockerService dockerService(@Autowired DockerClient dockerClient,
      @Autowired EnvProcessor envProcessor){
    return new DockerService(dockerClient, envProcessor);
  }

  @Bean
  public EgoDeploymentService egoDeploymentService(@Autowired DockerService dockerService){
    return new EgoDeploymentService(dockerService, new EgoSpecConverter("robnetwork"));
  }

  @Bean
  public DockerClient dockerClient(){
    val config = buildDockerClientConfig();
    return getInstance(config, buildDockerHttpClient(config));
  }

  private static DockerHttpClient buildDockerHttpClient(DockerClientConfig config){
    return new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .build();
  }

  private static DockerClientConfig buildDockerClientConfig(){
    return createDefaultConfigBuilder().build();
  }

}
