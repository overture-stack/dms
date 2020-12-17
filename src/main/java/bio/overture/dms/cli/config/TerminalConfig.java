package bio.overture.dms.cli.config;

import static jline.internal.Configuration.getBoolean;

import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.terminal.TerminalImpl;
import java.io.IOException;
import jline.console.ConsoleReader;
import lombok.val;
import org.beryx.textio.jline.JLineTextTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TerminalConfig {

  // TODO: make this configurable via BootstrapCommand
  private boolean ansi = true;
  // TODO: make this configurable via BootstrapCommand
  private boolean silent;

  @Bean
  @Profile("!test")
  public JLineTextTerminal jlineTextTerminal() {
    val textTerminal = new JLineTextTerminal();
    textTerminal.init();
    return textTerminal;
  }

  @Bean
  @Profile("test")
  public JLineTextTerminal jlineTestTerminal() throws IOException {
    val cr = new ConsoleReader(System.in, System.out);
    boolean expandEvents = getBoolean(ConsoleReader.JLINE_EXPAND_EVENTS, false);
    cr.setExpandEvents(expandEvents);
    val textTerminal = new JLineTextTerminal(cr);
    textTerminal.init();
    return textTerminal;
  }

  @Bean
  public Terminal terminal(@Autowired JLineTextTerminal textTerminal) {
    return new TerminalImpl(
        ansi, silent, textTerminal.getReader().getTerminal().getWidth(), textTerminal);
  }
}
