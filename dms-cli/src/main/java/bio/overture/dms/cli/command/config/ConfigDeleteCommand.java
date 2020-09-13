package bio.overture.dms.cli.command.config;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "rm",
    mixinStandardHelpOptions = true,
    description = "Delete the currently staged configuration" )
public class ConfigDeleteCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
