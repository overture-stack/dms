package bio.overture.dms.ego.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("ego.client")
public class EgoClientProperties {

  private final EgoTimeoutSecondsProperties timeoutSeconds = new EgoTimeoutSecondsProperties();
  private final RetryProperties retry = new RetryProperties();
}
