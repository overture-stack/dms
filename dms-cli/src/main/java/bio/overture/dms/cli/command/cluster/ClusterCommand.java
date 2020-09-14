package bio.overture.dms.cli.command.cluster;

import picocli.CommandLine.Command;

@Command(
    name = "cluster",
    mixinStandardHelpOptions = true,
    subcommands = {
        ClusterApplyCommand.class,
        ClusterDestroyCommand.class,
        ClusterGetCommand.class,
        ClusterRestartCommand.class,
        ClusterStartCommand.class,
        ClusterStopCommand.class,
        ClusterStatusCommand.class
    })
public class ClusterCommand { }
