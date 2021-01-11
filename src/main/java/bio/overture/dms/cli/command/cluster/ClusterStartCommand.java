package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.util.VersionProvider;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "start",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Start an existing cluster")
public class ClusterStartCommand implements Callable<Integer> {

  @Option(
      names = {"--skip-ego-init"},
      required = false,
      description = "Skip Ego initialization")
  private boolean skipEgoInit = false;

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
