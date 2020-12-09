package bio.overture.dms.domain.compose;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Healthcheck {

  private Boolean disable;

  @JsonDeserialize(using = DurationNsDeserializer.class)
  private Long interval;

  private Integer retries;

  @JsonDeserialize(using = StringOrArrayDeserializer.class)
  private List<String> test;

  @JsonDeserialize(using = DurationNsDeserializer.class)
  private Long timeout;

  @JsonDeserialize(using = DurationNsDeserializer.class)
  private Long startPeriod;
}
