package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import java.util.concurrent.Callable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "upgrade",
    aliases = {"up"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Upgrade the configuration version")
public class ConfigUpgradeCommand implements Callable<Integer> {

  @Option(
      names = {"--skip-interactive"},
      required = false,
      description =
          "Skip the interactive questionnaire and instead follow up with `dms config build` to build the configuration")
  private boolean skipInteractive = false;

  @ArgGroup(exclusive = true, multiplicity = "1")
  private ExclusiveVersion exclusiveVersion;

  static class ExclusiveVersion {
    @Option(
        names = {"--latest"},
        description = "Upgrade to the latest version",
        required = true)
    private boolean useLatest;

    @Option(
        names = {"--new-version"},
        description = "Upgrade to a specific version",
        required = true)
    private String newVersion;
  }

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
