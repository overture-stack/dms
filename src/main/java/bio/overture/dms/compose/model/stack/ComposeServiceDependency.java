package bio.overture.dms.compose.model.stack;

import static bio.overture.dms.compose.model.stack.DependencyTypes.SERVICE_STARTED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(NON_NULL)
public class ComposeServiceDependency {

  private String name;
  private DependencyTypes type = SERVICE_STARTED;
}
