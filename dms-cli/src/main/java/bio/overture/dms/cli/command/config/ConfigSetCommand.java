package bio.overture.dms.cli.command.config;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
    name = "set",
    aliases = {"s"},
    mixinStandardHelpOptions = true,
    description = "Set a configuration" )
public class ConfigSetCommand implements Callable<Integer> {

    @Option(names = { "-f", "--file" },
        required = true,
        description = "Configuration file to set")
    private File configFile;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
