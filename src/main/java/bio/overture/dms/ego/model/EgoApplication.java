package bio.overture.dms.ego.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EgoApplication {

  @NotBlank private String id;
  @NotBlank private String name;
  @NotBlank private String type;
  @NotBlank private String clientId;
  @NotBlank private String clientSecret;
  private String redirectUri;
  private String description;
  @NotBlank private String status;
}
