package bio.overture.dms.cli.config;

import bio.overture.dms.cli.properties.TerminalProperties;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.terminal.TerminalImpl;
import lombok.NonNull;
import org.beryx.textio.mock.MockTextTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestTerminalConfig {

  private final TerminalProperties terminalProperties;

  @Autowired
  public TestTerminalConfig(@NonNull TerminalProperties terminalProperties) {
    this.terminalProperties = terminalProperties;
  }

  @Bean
  public MockTextTerminal mockTextTerminal() {
    return new MockTextTerminal();
  }

  @Bean
  public Terminal terminal(@Autowired MockTextTerminal textTerminal) {
    return new TerminalImpl(
        terminalProperties.isAnsi(), terminalProperties.isSilent(), 80, textTerminal);
  }
}
