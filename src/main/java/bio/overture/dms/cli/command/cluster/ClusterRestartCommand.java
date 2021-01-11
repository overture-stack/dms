package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.util.VersionProvider;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(
    name = "restart",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Restart a cluster")
public class ClusterRestartCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
