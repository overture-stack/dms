package bio.overture.dms.version2.model;

import static bio.overture.dms.version2.model.DependencyTypes.SERVICE_STARTED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(NON_NULL)
public class ComposeServiceDependency {

  private String name;
  private DependencyTypes type = SERVICE_STARTED;
}
