package bio.overture.dms.cli.command.config;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

import bio.overture.dms.cli.model.enums.OutputFormats;
import bio.overture.dms.cli.util.VersionProvider;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "get",
    aliases = {"g"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Get the current configuration")
public class ConfigGetCommand implements Callable<Integer> {

  @Option(
      names = {"--show-secrets"},
      required = false,
      description = "Expose base64 encoded secrets")
  private boolean showSecrets = false;

  @Option(
      names = {"-o", "--output-format"},
      required = false,
      showDefaultValue = ALWAYS,
      description = "Specify output format: ${COMPLETION-CANDIDATES}")
  private OutputFormats outputFormat = OutputFormats.yaml;

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
