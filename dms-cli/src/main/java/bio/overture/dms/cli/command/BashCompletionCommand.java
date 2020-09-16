package bio.overture.dms.cli.command;

import bio.overture.dms.cli.util.VersionProvider;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static picocli.AutoComplete.bash;

@Component
@Command(
    name = "bash-completion",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Dump the bash-completion script")
public class BashCompletionCommand implements Callable<Integer> {

  /**
   * Dependencies
   */
  private final CommandLine commandLine;

  /**
   * Parameters
   */
  @Option(names = { "-n", "--script-name" },
      required = true,
      paramLabel = "STRING",
      description = "Name of the entry point command")
  private String scriptName;

  @Option(names = { "-o", "--output-file" },
      required = false,
      paramLabel = "FILE",
      description = "Path of output auto-complete script")
  private File outFile;

  @Autowired
  public BashCompletionCommand(@NonNull CommandLine commandLine) {
    this.commandLine = commandLine;
  }

  @Override
  public Integer call() throws Exception {
    /**
     * Note: removed output to file, since this can cause confusion with docker paths
     */
    System.out.println(bash(scriptName,commandLine));
    return 0;
  }
}
