package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "delete",
    aliases = {"del", "rm"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Delete the currently staged configuration" )
public class ConfigDeleteCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
