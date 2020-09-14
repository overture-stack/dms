package bio.overture.dms.cli;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import picocli.CommandLine;

@SpringBootTest
public class ContextTest {

  @Autowired
  private CommandLine commandLine;

  @Test
  public void testRob(){
    String[] args = {"config", "setup" , "--help"};
    commandLine.execute(args);
  }

}
