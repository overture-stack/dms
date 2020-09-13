package bio.overture.dms.cli.command.config;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
    name = "version",
    mixinStandardHelpOptions = true,
    description = "Display the configured dms version" )
public class ConfigVersionCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
