package bio.overture.dms.cli.command.spec;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "status",
    aliases = {"st"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Show the status of the current specification" )
public class SpecStatusCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
