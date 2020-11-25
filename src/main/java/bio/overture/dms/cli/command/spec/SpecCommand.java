package bio.overture.dms.cli.command.spec;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

@Command(
    name = "spec",
    aliases = {"sp"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    subcommands = {
      SpecCheckCommand.class,
      SpecConfigCommand.class,
      SpecGetCommand.class,
      SpecSetCommand.class,
      SpecDeleteCommand.class,
      SpecStatusCommand.class,
      SpecUpgradeCommand.class,
      SpecVersionCommand.class
    })
public class SpecCommand {}
