package bio.overture.dms.cli.command.cluster;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "restart",
    mixinStandardHelpOptions = true,
    description = "Restart a cluster" )
public class ClusterRestartCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}