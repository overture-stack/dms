package bio.overture.dms.cli;

import bio.overture.dms.cli.command.DmsCommand;
import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.util.List;

public class ContextTest {

  @Test
  public void testRob(){
    String[] args = {"config", "-h"};
    new CommandLine(DmsCommand.class).execute(args);
  }

}
