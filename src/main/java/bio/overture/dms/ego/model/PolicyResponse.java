package bio.overture.dms.ego.model;

import lombok.Data;

@Data
public class PolicyResponse {

  private String id;
  private String name;
  private PermissionMasks mask;
}
