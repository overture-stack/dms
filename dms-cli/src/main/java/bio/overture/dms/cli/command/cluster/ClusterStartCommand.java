package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.model.enums.OutputFormats;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
    name = "start",
    mixinStandardHelpOptions = true,
    description = "Start an existing cluster" )
public class ClusterStartCommand implements Callable<Integer> {

    @Option(names = { "--skip-ego-init" },
        required = false,
        description = "Skip Ego initialization")
    private boolean skipEgoInit = false;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
