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
public class ListGroupPermissionsRequest {

  @NotNull private String policyId;
  private String query;
  private Integer limit;
  private Integer offset;
}
