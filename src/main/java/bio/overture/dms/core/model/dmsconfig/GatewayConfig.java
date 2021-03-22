package bio.overture.dms.core.model.dmsconfig;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URL;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  private String sslDir = "/etc/ssl/dms";
}
