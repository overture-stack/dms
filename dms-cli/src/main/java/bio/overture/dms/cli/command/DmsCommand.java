package bio.overture.dms.cli.command;

import java.util.concurrent.Callable;

import bio.overture.dms.cli.command.cluster.ClusterCommand;
import bio.overture.dms.cli.command.config.ConfigCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Option;

@Slf4j
@Component
@RequiredArgsConstructor
@CommandLine.Command(
    name = "dms",
    mixinStandardHelpOptions = true,
    subcommands = {
        SummaryCommand.class,
        ConfigCommand.class,
        ClusterCommand.class,
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
