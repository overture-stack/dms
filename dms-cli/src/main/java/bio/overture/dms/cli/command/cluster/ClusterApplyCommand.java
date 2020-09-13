package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.model.enums.OutputFormats;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

@Command(
    name = "apply",
    mixinStandardHelpOptions = true,
    description = "Deploy the staged configuration to the cluster" )
public class ClusterApplyCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
