package bio.overture.dms.cli.command;

import bio.overture.dms.cli.command.cluster.ClusterCommand;
import bio.overture.dms.cli.command.config.ConfigCommand;
import bio.overture.dms.cli.util.VersionProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@RequiredArgsConstructor
@Command(
    name = "dms",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    subcommands = {
      SummaryCommand.class,
      ConfigCommand.class,
      ClusterCommand.class,
      BashCompletionCommand.class
    },
    description = "DMS command")
public class DmsCommand {}
