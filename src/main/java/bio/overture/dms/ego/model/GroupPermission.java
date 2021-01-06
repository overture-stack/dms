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
public class GroupPermission {

  @NotBlank private String id;
  @NotBlank private PermissionMasks accessLevel;
  @NotNull private EgoGroup group;
  @NotNull private EgoPolicy policy;
}
