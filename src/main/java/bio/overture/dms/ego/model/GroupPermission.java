package bio.overture.dms.ego.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPermission {

  @NotBlank private String id;
  @NotBlank private PermissionMasks accessLevel;
  @NotNull private EgoGroup owner;
  @NotNull private EgoPolicy policy;
}
