package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

@Command(
    name = "cluster",
    aliases = {"cl"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    subcommands = {
      ClusterApplyCommand.class,
      ClusterDestroyCommand.class,
      ClusterGetCommand.class,
      ClusterRestartCommand.class,
      ClusterStartCommand.class,
      ClusterStopCommand.class,
      ClusterStatusCommand.class
    })
public class ClusterCommand {}
