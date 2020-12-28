package bio.overture.dms.cli.command;

import static bio.overture.dms.util.TestTextTerminal.createTestTextTerminal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import bio.overture.dms.cli.DmsConfigStore;
import bio.overture.dms.cli.command.config.ConfigGetCommand;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.terminal.TerminalImpl;
import bio.overture.dms.util.TestTextTerminal;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Disabled("need to mock dmsConfigStore properly")
public class ConfigCommandTest {

  /** Dependencies */
  @Autowired private DmsConfigStore dmsConfigStore;

  /** State */
  private TestTextTerminal testTextTerminal;

  private Terminal terminal;

  @BeforeEach
  public void beforeTest() {
    this.testTextTerminal = createTestTextTerminal();
    this.terminal =
        TerminalImpl.builder()
            .ansi(true)
            .silent(false)
            .terminalWidth(80)
            .textTerminal(testTextTerminal)
            .build();
  }

  @Test
  @SneakyThrows
  public void testConfigGet() {
    val cmd = new ConfigGetCommand(dmsConfigStore, terminal);
    val exitCode = cmd.call();
    val output = testTextTerminal.getOutput(false);
    assertFalse(output.isBlank());
    assertEquals(0, exitCode);
  }
}
