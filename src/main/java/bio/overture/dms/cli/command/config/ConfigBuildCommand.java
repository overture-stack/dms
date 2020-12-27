package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.DmsConfigStore;
import bio.overture.dms.cli.questionnaire.DmsQuestionnaire;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.util.VersionProvider;
import java.util.concurrent.Callable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Slf4j
@Command(
    name = "build",
    aliases = {"bu"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Interactively build a configuration")
public class ConfigBuildCommand implements Callable<Integer> {

  private final DmsConfigStore dmsConfigStore;
  private final Terminal t;
  private final DmsQuestionnaire dmsQuestionnaire;

  @Autowired
  public ConfigBuildCommand(
      @NonNull DmsConfigStore dmsConfigStore,
      @NonNull Terminal terminal,
      @NonNull DmsQuestionnaire dmsQuestionnaire) {
    this.dmsConfigStore = dmsConfigStore;
    this.t = terminal;
    this.dmsQuestionnaire = dmsQuestionnaire;
  }

  @Option(
      names = {"--skip-answered"},
      required = false,
      description = "Skip previously answered questions, and jump to the first unanswered question")
  private boolean skipAnswered = false;

  @Option(
      names = {"--skip-system-check"},
      required = false,
      description = "Skip the system check")
  private boolean skipSystemCheck = false;

  @Override
  public Integer call() throws Exception {
    t.printStatusLn("Starting interactive configuration");
    // TODO: Fix this so that the storedDmsConfig is input into the buildDmsConfig method
    dmsConfigStore.apply(storedDmsConfig -> dmsQuestionnaire.buildDmsConfig());
    t.printStatusLn("Wrote config file to %s", dmsConfigStore.getDmsConfigFilePath());
    return 0;
  }
}
