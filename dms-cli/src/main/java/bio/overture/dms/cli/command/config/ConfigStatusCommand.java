package bio.overture.dms.cli.command.config;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "status",
    aliases = {"st"},
    mixinStandardHelpOptions = true,
    description = "Show the status of the currently staged configuration" )
public class ConfigStatusCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
