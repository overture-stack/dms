package bio.overture.dms.ego.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EgoApplication {

  @NotNull private String id;
  @NotNull private String name;
  @NotNull private String type;
  @NotNull private String clientId;
  @NotNull private String clientSecret;
  private String redirectUri;
  private String description;
  @NotNull private String status;
}
