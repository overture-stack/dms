package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
    name = "setup",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Interactively create a configuration" )
public class ConfigSetupCommand implements Callable<Integer> {

    @Option(names = { "--skip-answered" },
        required = false,
        description = "Skip previously answered questions, and jump to the first unanswered question")
    private boolean skipAnswered = false;

    @Option(names = { "--skip-system-check" },
        required = false,
        description = "Skip the system check")
    private boolean skipSystemCheck = false;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
