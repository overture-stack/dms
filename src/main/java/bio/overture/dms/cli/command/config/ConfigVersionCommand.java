package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(
    name = "version",
    aliases = {"v"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Display the configuration version")
public class ConfigVersionCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
