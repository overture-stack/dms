package bio.overture.dms.ego.properties;

import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetryProperties {

  @Min(1)
  private int initialSeconds = 2;

  @Min(1)
  private int maxSeconds = 1 << 7;

  @Min(1)
  private double multiplier = 2.0;

  @Min(1)
  private int maxAttempts;
}
