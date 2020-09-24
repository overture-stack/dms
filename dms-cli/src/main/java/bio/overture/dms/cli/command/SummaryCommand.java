package bio.overture.dms.cli.command;

import java.util.concurrent.Callable;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

@Command(
    name = "summary",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Display a summary of the cluster and configuration")
public class SummaryCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
