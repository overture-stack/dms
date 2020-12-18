package bio.overture.dms.compose.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

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
