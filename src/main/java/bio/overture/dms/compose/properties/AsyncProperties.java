package bio.overture.dms.compose.properties;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("async")
public class AsyncProperties {

  private int threadCount;

  @Min(10)
  private long timeoutSeconds;

  public Duration getTimeoutDuration() {
    return Duration.of(timeoutSeconds, SECONDS);
  }
}
