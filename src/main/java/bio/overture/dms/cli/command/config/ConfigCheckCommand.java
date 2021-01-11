package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(
    name = "check",
    aliases = {"ch"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Check if the config is ok to deploy")
public class ConfigCheckCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
