package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "status",
    aliases = {"st"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Show the status of the currently staged configuration" )
public class ConfigStatusCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
