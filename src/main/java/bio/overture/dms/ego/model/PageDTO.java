package bio.overture.dms.ego.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {
  private int count;
  private int limit;
  private int offset;
  private List<T> resultSet;
}
