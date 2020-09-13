package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.model.enums.OutputFormats;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
    name = "get",
    mixinStandardHelpOptions = true,
    description = "Get the currently staged configuration" )
public class ConfigGetCommand implements Callable<Integer> {

    @Option(names = { "--show-secrets" },
        required = false,
        description = "Expose base64 encoded secrets")
    private boolean showSecrets= false;

    @Option(names = { "-o", "--output-format" },
        required = false,
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
        description = "Specify output format: ${COMPLETION-CANDIDATES}")
    private OutputFormats outputFormat = OutputFormats.yaml;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
