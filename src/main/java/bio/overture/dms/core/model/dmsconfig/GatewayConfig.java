package bio.overture.dms.core.model.dmsconfig;

import static bio.overture.dms.cli.model.Constants.DockerImagesConstants.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

  @JsonIgnore private String image = GHCR_IO_OVERTURE_STACK_DMS_GATEWAY + ":" + DMS_GATEWAY_TAG;

  @JsonIgnore
  private String imageSecure = GHCR_IO_OVERTURE_STACK_DMS_GATEWAY_SECURE + ":" + DMS_GATEWAY_TAG;
}
