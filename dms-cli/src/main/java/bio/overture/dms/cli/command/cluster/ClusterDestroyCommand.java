package bio.overture.dms.cli.command.cluster;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

@Command(
    name = "destroy",
    mixinStandardHelpOptions = true,
    description = "Destroy the cluster" )
public class ClusterDestroyCommand implements Callable<Integer> {

    @Option(names = { "-v", "--volumes" },
        required = false,
        showDefaultValue = ALWAYS,
        description = "Additionally destroy volumes")
    private boolean destroyVolumes = false;

    @Option(names = { "-f", "--force" },
        required = false,
        showDefaultValue = ALWAYS,
        description = "Forcefully destroy")
    private boolean force = false;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
