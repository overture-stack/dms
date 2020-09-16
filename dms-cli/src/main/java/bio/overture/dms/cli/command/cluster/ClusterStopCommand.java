package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "stop",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Stop a running cluster" )
public class ClusterStopCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
