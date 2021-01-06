package bio.overture.dms.compose.config;

import bio.overture.dms.compose.manager.ComposeStackGraphGenerator;
import bio.overture.dms.compose.properties.ComposeProperties;
import bio.overture.dms.swarm.service.SwarmService;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComposeConfig {

  private final ComposeProperties composeProperties;
  private final SwarmService swarmService;

  @Autowired
  public ComposeConfig(
      @NonNull ComposeProperties composeProperties, @NonNull SwarmService swarmService) {
    this.composeProperties = composeProperties;
    this.swarmService = swarmService;
  }

  @Bean
  public ComposeStackGraphGenerator composeStackGraphGenerator() {
    val generator = new ComposeStackGraphGenerator(composeProperties.getNetwork(), swarmService);
    generator.init();
    return generator;
  }
}
