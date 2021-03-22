package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

@Command(
    name = "config",
    aliases = {"co"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    subcommands = {
      ConfigBuildCommand.class,
      ConfigGetCommand.class,
    })
public class ConfigCommand {}
