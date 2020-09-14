package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "version",
    aliases = {"v"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Display the configured dms version" )
public class ConfigVersionCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
