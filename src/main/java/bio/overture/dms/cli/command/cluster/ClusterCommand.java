package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

@Command(
    name = "cluster",
    aliases = {"cl"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    subcommands = {
      ClusterStartCommand.class,
      ClusterStopCommand.class,
      ClusterDestroyCommand.class,
    })
public class ClusterCommand {}
