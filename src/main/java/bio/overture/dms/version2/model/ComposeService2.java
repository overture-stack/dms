package bio.overture.dms.version2.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dockerjava.api.model.ServiceSpec;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(NON_NULL)
public class ComposeService2 {

  private String name;

  @JsonProperty("ServiceSpec")
  @JsonIgnoreProperties
  private ServiceSpec serverSpec;

  private List<ComposeServiceDependency> dependencies = new ArrayList<>();
}
