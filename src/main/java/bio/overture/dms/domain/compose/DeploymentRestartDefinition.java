package bio.overture.dms.domain.compose;

import static bio.overture.dms.domain.compose.RetryCondition.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonInclude(NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DeploymentRestartDefinition {

  private RetryCondition condition = ANY;

  @JsonDeserialize(using = DurationNsDeserializer.class)
  private String delay; // Duration

  private Integer maxAttempts;

  @JsonDeserialize(using = DurationNsDeserializer.class)
  private String window; // Duration
}
