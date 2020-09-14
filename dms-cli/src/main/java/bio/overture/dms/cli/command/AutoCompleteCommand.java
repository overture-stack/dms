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
    name = "auto-complete",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
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
      required = false,
      paramLabel = "FILE",
      description = "Path of output auto-complete script")
  private File outFile;

  @Autowired
  public AutoCompleteCommand(@NonNull CommandLine commandLine) {
    this.commandLine = commandLine;
  }

  @Override
  public Integer call() throws Exception {
    if (isNull(outFile)){
      System.out.println(bash(scriptName,commandLine));
    } else {
      //TODO check parent dir exists
      bash(scriptName,outFile, null, commandLine);
      System.out.println(format("Wrote bash completion for script \"%s\" to \"%s\"", scriptName, outFile.getName()));
    }
    return 0;
  }
}
