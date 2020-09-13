package bio.overture.dms.cli.command.config;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
    name = "upgrade",
    mixinStandardHelpOptions = true,
    description = "Upgrade the version of the software" )
public class ConfigUpgradeCommand implements Callable<Integer> {

    @Option(names = { "--skip-interactive" },
        required = false,
        description = "Skip the interactive questionaire and instead follow up with `dms config setup` to complete the configuration")
    private boolean skipInteractive= false;

    @ArgGroup(exclusive = true, multiplicity = "1")
    private ExclusiveVersion exclusiveVersion;

    static class ExclusiveVersion {
        @Option(names = { "--latest" },
            description = "Upgrade to the latest version", required = true)
        private boolean useLatest;

        @Option(names = { "--new-version" },
            description = "Upgrade to a specific version", required = true)
        private String newVersion;
    }

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
