package bio.overture.dms.ego.model;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EgoGroup {

  @NotBlank private String id;
  @NotBlank private String name;
  @NotBlank private String status;
  private String description;
}
