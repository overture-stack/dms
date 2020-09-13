package bio.overture.dms.cli.command;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Slf4j
@Component
@RequiredArgsConstructor
@CommandLine.Command(
    name = "dms",
    mixinStandardHelpOptions = true,
    subcommands = {
        SummaryCommand.class,
        AutoCompleteCommand.class
    },
    description = "DMS command")
public class DmsCommand implements Callable<Integer> {

  @Override
  public Integer call() {
    CommandLine.usage(this, System.out);
    return 0;
  }
}
