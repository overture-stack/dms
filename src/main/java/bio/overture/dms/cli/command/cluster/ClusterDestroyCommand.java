package bio.overture.dms.cli.command.cluster;

import static bio.overture.dms.cli.model.Constants.CommandsQuestions.CONFIRM_DESTROY;
import static bio.overture.dms.cli.model.enums.QuestionProfiles.WARNING;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

import bio.overture.dms.cli.DmsConfigStore;
import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.util.VersionProvider;
import bio.overture.dms.compose.deployment.DmsComposeManager;
import java.util.concurrent.Callable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Setter
@Component
@Command(
    name = "destroy",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Destroy the cluster and ALL the data.")
public class ClusterDestroyCommand implements Callable<Integer> {


  /** Dependencies */
  private final Terminal t;

  private final QuestionFactory questionFactory;
  private final DmsComposeManager dmsComposeManager;
  private final DmsConfigStore dmsConfigStore;

  @CommandLine.Option(
      names = {"-f", "--force"},
      required = false,
      showDefaultValue = ALWAYS,
      description = "Forcefully destroy volumes without asking first")
  private boolean force = false;

  @Builder
  @Autowired
  public ClusterDestroyCommand(
      @NonNull Terminal terminal,
      @NonNull QuestionFactory questionFactory,
      @NonNull DmsComposeManager dmsComposeManager,
      @NonNull DmsConfigStore dmsConfigStore) {
    this.t = terminal;
    this.questionFactory = questionFactory;
    this.dmsComposeManager = dmsComposeManager;
    this.dmsConfigStore = dmsConfigStore;
  }

  @Override
  public Integer call() throws Exception {
    val result = dmsConfigStore.findStoredConfig();
    if (result.isPresent()) {
      t.printStatusLn("Starting cluster destruction: force=%s", force);
      val isConfirmed = confirmVolumesDeletion();
      if (!isConfirmed) {
        t.printStatusLn("Cluster destruction canceled.");
        return 2;
      }
      dmsComposeManager.destroy(result.get(), true);
      t.printStatusLn("Finished cluster destruction");
      return 0;
    }

    t.printErrorLn("Could not find DMS configuration: %s", dmsConfigStore.getDmsConfigFilePath());
    return 1;
  }

  private boolean confirmVolumesDeletion() {
    boolean askQuestion = !force;
    boolean confirmedVolumeDestruction = force;
    if (askQuestion) {
      confirmedVolumeDestruction =
          questionFactory
              .newSingleQuestion(
                  WARNING,
                  Boolean.class,
                  CONFIRM_DESTROY,
                  true,
                  false)
              .getAnswer();
    }
    if (confirmedVolumeDestruction) {
      t.printStatus("Forcefully destroying all volumes!");
    }
    return confirmedVolumeDestruction;
  }
}
