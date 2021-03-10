package bio.overture.dms.core.model.dmsconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.net.URL;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class GatewayConfig {
  @Builder.Default private boolean pathBased = true;
  @Min(value = 2000)
  @Builder.Default
  private int hostPort = 80;
  @NotNull private URL url;
}
