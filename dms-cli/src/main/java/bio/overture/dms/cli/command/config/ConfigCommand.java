package bio.overture.dms.cli.command.config;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
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
public class ConfigCommand { }
