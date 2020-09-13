package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.command.config.ConfigCheckCommand;
import bio.overture.dms.cli.command.config.ConfigDeleteCommand;
import bio.overture.dms.cli.command.config.ConfigGetCommand;
import bio.overture.dms.cli.command.config.ConfigSetCommand;
import bio.overture.dms.cli.command.config.ConfigSetupCommand;
import bio.overture.dms.cli.command.config.ConfigStatusCommand;
import bio.overture.dms.cli.command.config.ConfigUpgradeCommand;
import bio.overture.dms.cli.command.config.ConfigVersionCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
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
public class ClusterCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        CommandLine.usage(this, System.out);
        return 0;
    }
}
