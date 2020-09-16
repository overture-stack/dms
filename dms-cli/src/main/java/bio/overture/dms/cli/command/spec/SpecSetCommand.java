package bio.overture.dms.cli.command.spec;

import bio.overture.dms.cli.util.VersionProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
    name = "set",
    aliases = {"s"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Set a specification" )
public class SpecSetCommand implements Callable<Integer> {

    @Option(names = { "-f", "--file" },
        required = true,
        description = "Spec file to set")
    private File specFile;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
