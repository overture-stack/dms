package bio.overture.dms.cli.config;

import bio.overture.dms.cli.properties.TerminalProperties;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.terminal.TerminalImpl;
import lombok.NonNull;
import lombok.val;
import org.beryx.textio.jline.JLineTextTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class ProdTerminalConfig {

  private final TerminalProperties terminalProperties;

  @Autowired
  public ProdTerminalConfig(@NonNull TerminalProperties terminalProperties) {
    this.terminalProperties = terminalProperties;
  }

  @Bean
  public JLineTextTerminal jlineTextTerminal() {
    val textTerminal = new JLineTextTerminal();
    textTerminal.init();
    return textTerminal;
  }

  @Bean
  public Terminal terminal(@Autowired JLineTextTerminal textTerminal) {
    return new TerminalImpl(
        terminalProperties.isAnsi(),
        terminalProperties.isSilent(),
        textTerminal.getReader().getTerminal().getWidth(),
        textTerminal);
  }
}
