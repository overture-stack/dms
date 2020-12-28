package bio.overture.dms.swarm.config;

import bio.overture.dms.swarm.properties.SwarmProperties;
import bio.overture.dms.swarm.service.SwarmService;
import bio.overture.dms.swarm.service.SwarmSpecService;
import com.github.dockerjava.api.DockerClient;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwarmConfig {

  private final SwarmProperties swarmProperties;

  @Autowired
  public SwarmConfig(@NonNull SwarmProperties swarmProperties) {
    this.swarmProperties = swarmProperties;
  }

  /**
   * If auto-initialize is set in the swarm config, then after constructing the SwarmService,
   * initialize it
   */
  @Bean
  @Autowired
  public SwarmService swarmService(DockerClient dockerClient, SwarmSpecService swarmSpecService) {
    val swarmService = new SwarmService(dockerClient, swarmSpecService);
    if (swarmProperties.isAutoInitialize()) {
      swarmService.initializeSwarm();
    }
    return swarmService;
  }
}
