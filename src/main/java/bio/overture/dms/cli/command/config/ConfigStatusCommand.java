package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(
    name = "status",
    aliases = {"st"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Show the status of the current configuration")
public class ConfigStatusCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
