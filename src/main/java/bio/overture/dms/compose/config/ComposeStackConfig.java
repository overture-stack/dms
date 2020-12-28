package bio.overture.dms.compose.config;

import bio.overture.dms.compose.properties.ComposeStackProperties;
import bio.overture.dms.compose.service.ComposeStackGraphGenerator;
import bio.overture.dms.swarm.service.SwarmService;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComposeStackConfig {

  private final ComposeStackProperties composeStackProperties;
  private final SwarmService swarmService;

  @Autowired
  public ComposeStackConfig(
      @NonNull ComposeStackProperties composeStackProperties, @NonNull SwarmService swarmService) {
    this.composeStackProperties = composeStackProperties;
    this.swarmService = swarmService;
  }

  @Bean
  public ComposeStackGraphGenerator composeStackGraphGenerator() {
    val generator =
        new ComposeStackGraphGenerator(composeStackProperties.getNetwork(), swarmService);
    generator.init();
    return generator;
  }
}