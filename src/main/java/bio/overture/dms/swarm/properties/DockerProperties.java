package bio.overture.dms.swarm.properties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("docker")
public class DockerProperties {

  @NotBlank private String host;
  @NotNull private Boolean runAs;
  @NotNull private String dmsHomeHostPath;
}
