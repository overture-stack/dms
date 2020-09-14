package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "status",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Display the cluster status" )
public class ClusterStatusCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
