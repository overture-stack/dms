package bio.overture.dms.cli;

import bio.overture.dms.cli.terminal.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class TerminalTest {

  @Autowired Terminal terminal;

  @Test
  public void testTerminal() {
    terminal
        .printStatus("somestatus")
        .printWarning("some warning")
        .printLink("some link")
        .printLabel("some label")
        .printError("some error")
        .println()
        .println("print ln reg")
        .printLine()
        .println();
  }
}
