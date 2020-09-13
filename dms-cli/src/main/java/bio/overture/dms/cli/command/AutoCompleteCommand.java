package bio.overture.dms.cli.command;

import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

import static picocli.AutoComplete.bash;

@Component
@Command(
    name = "auto-complete",
    mixinStandardHelpOptions = true,
    description = "Dump the auto-complete script to a file")
public class AutoCompleteCommand implements Callable<Integer> {

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
      required = true,
      paramLabel = "FILE",
      description = "Path of output auto-complete script")
  private File outFile;

  @Autowired
  public AutoCompleteCommand(@NonNull CommandLine commandLine) {
    this.commandLine = commandLine;
  }

  @Override
  public Integer call() throws Exception {
    bash(scriptName,outFile, null, commandLine);
    return 0;
  }
}
