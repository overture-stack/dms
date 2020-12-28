package bio.overture.dms.cli.config;

import bio.overture.dms.cli.properties.TerminalProperties;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.terminal.TerminalImpl;
import lombok.NonNull;
import lombok.val;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
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
  public TextIO textIO(@NonNull TextTerminal<?> textTerminal) {
    return new TextIO(textTerminal);
  }

  @Bean
  public JLineTextTerminal jlineTextTerminal() {
    val textTerminal = new JLineTextTerminal();
    textTerminal.init();
    return textTerminal;
  }

  @Bean
  public Terminal terminal(@Autowired JLineTextTerminal textTerminal) {
    return TerminalImpl.builder()
        .ansi(terminalProperties.isAnsi())
        .silent(terminalProperties.isSilent())
        .terminalWidth(textTerminal.getReader().getTerminal().getWidth())
        .textTerminal(textTerminal)
        .build();
  }
}
