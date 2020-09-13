package bio.overture.dms.cli.command.config;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "config",
    mixinStandardHelpOptions = true,
    subcommands = {
        ConfigCheckCommand.class,
        ConfigSetupCommand.class,
        ConfigGetCommand.class,
        ConfigSetCommand.class,
        ConfigDeleteCommand.class,
        ConfigStatusCommand.class,
        ConfigUpgradeCommand.class,
        ConfigVersionCommand.class
    })
public class ConfigCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        CommandLine.usage(this, System.out);
        return 0;
    }
}
