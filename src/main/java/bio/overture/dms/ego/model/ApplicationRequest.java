package bio.overture.dms.ego.model;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {

  @NotNull private String name;
  @NotNull private String type;
  @NotNull private String clientId;
  @NotNull private String clientSecret;
  private String redirectUri;
  private String description;
  @NotNull private String status;
}
