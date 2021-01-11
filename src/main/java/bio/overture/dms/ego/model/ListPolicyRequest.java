package bio.overture.dms.ego.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListPolicyRequest {
  private Integer limit;
  private Integer offset;
  private String query;
}
