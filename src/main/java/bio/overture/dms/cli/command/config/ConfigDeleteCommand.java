package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(
    name = "delete",
    aliases = {"del", "rm"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Delete the current configuration")
public class ConfigDeleteCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
