package bio.overture.dms.cli;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import picocli.CommandLine;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class ContextTest {

  @Autowired private CommandLine commandLine;

  @Test
  public void testContext() {
    String[] args = {"bash-completion", "-n", "dms"};
    commandLine.execute(args);
  }
}
