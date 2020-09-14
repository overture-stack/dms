package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

@Command(
    name = "config",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
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
