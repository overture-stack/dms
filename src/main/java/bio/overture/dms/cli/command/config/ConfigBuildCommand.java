package bio.overture.dms.cli.command.config;

import static bio.overture.dms.cli.model.Constants.MESSAGES.CONFIGURATION_SAVED_MSG;
import static bio.overture.dms.cli.model.Constants.MESSAGES.PRE_REQ_NOTE;

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

  @Override
  public Integer call() throws Exception {
    t.print(PRE_REQ_NOTE);
    t.printStatusLn("Starting interactive configuration...");
    dmsConfigStore.apply(dmsQuestionnaire::buildDmsConfig);
    t.printStatusLn(CONFIGURATION_SAVED_MSG, dmsConfigStore.getDmsConfigFilePath());
    return 0;
  }
}
