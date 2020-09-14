package bio.overture.dms.cli.command.spec;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
    name = "config",
    aliases = {"co"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Interactively configure a spec" )
public class SpecConfigCommand implements Callable<Integer> {

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
