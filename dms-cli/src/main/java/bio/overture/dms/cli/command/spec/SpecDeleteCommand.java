package bio.overture.dms.cli.command.spec;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "delete",
    aliases = {"del", "rm"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Delete the currently spec" )
public class SpecDeleteCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
