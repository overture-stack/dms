package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import java.io.File;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "set",
    aliases = {"s"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Set a configuration")
public class ConfigSetCommand implements Callable<Integer> {

  @Option(
      names = {"-f", "--file"},
      required = true,
      description = "Config file to set")
  private File configFile;

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
