package bio.overture.dms.cli.config;

import static bio.overture.dms.util.TestTextTerminal.createTestTextTerminal;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import bio.overture.dms.cli.properties.TerminalProperties;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.terminal.TerminalImpl;
import bio.overture.dms.util.TestTextTerminal;
import lombok.NonNull;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Configuration
@Profile("test")
public class TestTerminalConfig {

  private final TerminalProperties terminalProperties;

  @Autowired
  public TestTerminalConfig(@NonNull TerminalProperties terminalProperties) {
    this.terminalProperties = terminalProperties;
  }

  @Bean
  public TextIO textIO(@NonNull TextTerminal<?> textTerminal) {
    return new TextIO(textTerminal);
  }

  /**
   * Note: Reusing the same TestTextTerminal between different tests is not safe. TestTextTerminal
   * is mutable and therefore not thread safe. A new instance should be created everytime a test is
   * run. This bean exists only to satisfy loading of the ApplicationContext
   */
  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public TestTextTerminal testTextTerminal() {
    return createTestTextTerminal();
  }

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public Terminal terminal(@Autowired TestTextTerminal textTerminal) {
    return TerminalImpl.builder()
        .ansi(terminalProperties.isAnsi())
        .silent(terminalProperties.isSilent())
        .terminalWidth(80)
        .textTerminal(textTerminal)
        .build();
  }
}
