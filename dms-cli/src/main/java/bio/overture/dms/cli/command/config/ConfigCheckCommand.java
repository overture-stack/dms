package bio.overture.dms.cli.command.config;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "check",
    aliases = {"ch"},
    mixinStandardHelpOptions = true,
    description = "Check if the staged configuration is ok to deploy" )
public class ConfigCheckCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
