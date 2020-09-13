package bio.overture.dms.cli.command;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Command(
    name = "summary",
    mixinStandardHelpOptions = true,
    description = "Display a summary of the cluster and configuration")
public class SummaryCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
