package bio.overture.dms.domain.compose;

import static bio.overture.dms.domain.compose.FailureActions.PAUSE;
import static bio.overture.dms.domain.compose.StartOrders.STOP_FIRST;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonInclude(NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RetryConfig {

  private Integer parallelism;

  @JsonDeserialize(using = DurationNsDeserializer.class)
  private Long delay; // Duration

  private FailureActions failureAction = PAUSE;

  @JsonDeserialize(using = DurationNsDeserializer.class)
  private Long monitor; // Duration

  private Float maxFailureRatio;
  private StartOrders order = STOP_FIRST;
}
