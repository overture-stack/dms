package bio.overture.dms.ego.properties;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EgoTimeoutSecondsProperties {

  @Min(1)
  private int call = 10;

  @Min(1)
  private int connect = 10;

  @Min(1)
  private int read = 10;

  @Min(1)
  private int write = 10;

  public Duration getCallDuration() {
    return Duration.of(call, SECONDS);
  }

  public Duration getConnectDuration() {
    return Duration.of(connect, SECONDS);
  }

  public Duration getReadDuration() {
    return Duration.of(read, SECONDS);
  }

  public Duration getWriteDuration() {
    return Duration.of(write, SECONDS);
  }
}
