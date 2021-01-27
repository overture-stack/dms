package bio.overture.dms.ego.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationPermission {

  private String id;
  private EgoPolicy policy;
  private PermissionMasks accessLevel;
  private EgoApplication owner;
}
