package bio.overture.dms.cli.command.cluster;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
    name = "stop",
    mixinStandardHelpOptions = true,
    description = "Stop a running cluster" )
public class ClusterStopCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
