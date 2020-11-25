package bio.overture.dms.cli.command.spec;

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
    description = "Upgrade the spec version")
public class SpecUpgradeCommand implements Callable<Integer> {

  @Option(
      names = {"--skip-interactive"},
      required = false,
      description =
          "Skip the interactive questionaire and instead follow up with `dms spec config` to complete the spec configuration")
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
