package bio.overture.dms.ego.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {

  @NotBlank private String policyId;
  @NotBlank private PermissionMasks mask;

}
