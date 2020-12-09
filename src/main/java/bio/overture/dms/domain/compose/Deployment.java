package bio.overture.dms.domain.compose;

import static bio.overture.dms.domain.compose.DeployModes.REPLICATED;
import static bio.overture.dms.domain.compose.EndpointModes.VIP;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonInclude(NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Deployment {

  private DeployModes mode = REPLICATED;
  private EndpointModes endpointMode = VIP;
  private Integer replicas;
  private RetryConfig rollbackConfig;
  private RetryConfig updateConfig;
  private Resources resources;
  private DeploymentRestartDefinition restartPolicy;
}
