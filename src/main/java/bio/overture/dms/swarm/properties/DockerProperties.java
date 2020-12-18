package bio.overture.dms.swarm.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Component
@ConfigurationProperties("docker")
public class DockerProperties {

  @NotBlank private String host;

}
