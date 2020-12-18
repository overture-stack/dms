package bio.overture.dms.cli.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("terminal")
public class TerminalProperties {

  private boolean silent;
  private boolean ansi;
}
