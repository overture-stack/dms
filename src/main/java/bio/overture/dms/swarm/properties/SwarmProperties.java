package bio.overture.dms.swarm.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("swarm")
public class SwarmProperties {
  private boolean autoInitialize;
}
