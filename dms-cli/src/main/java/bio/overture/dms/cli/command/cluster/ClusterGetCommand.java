package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.model.enums.OutputFormats;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

@Command(
    name = "get",
    mixinStandardHelpOptions = true,
    description = "Get currently deployed configuration" )
public class ClusterGetCommand implements Callable<Integer> {

    @Option(names = { "--show-secrets" },
        required = false,
        showDefaultValue = ALWAYS,
        description = "Expose base64 encoded secrets")
    private boolean showSecrets= false;

    @Option(names = { "-o", "--output-format" },
        required = false,
        showDefaultValue = ALWAYS,
        description = "Specify output format: ${COMPLETION-CANDIDATES}")
    private OutputFormats outputFormat = OutputFormats.yaml;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
