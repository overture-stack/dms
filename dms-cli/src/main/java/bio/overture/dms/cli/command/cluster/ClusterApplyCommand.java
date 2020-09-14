package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "apply",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Deploy the staged configuration to the cluster" )
public class ClusterApplyCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
