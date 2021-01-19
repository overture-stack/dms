package bio.overture.dms.cli.command.cluster;

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
    description = "Destroy the cluster")
public class ClusterDestroyCommand implements Callable<Integer> {

  /** Dependencies */
  private final Terminal t;

  private final QuestionFactory questionFactory;
  private final DmsComposeManager dmsComposeManager;
  private final DmsConfigStore dmsConfigStore;

  /** CLI Options */
  @CommandLine.Option(
      names = {"-v", "--volumes"},
      required = false,
      showDefaultValue = ALWAYS,
      description = "Additionally destroy volumes")
  private boolean destroyVolumes = false;

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
      t.printStatusLn(
          "Starting cluster destruction: force=%s  destroyVolumes=%s", force, destroyVolumes);
      val resolvedDestroyVolumes = resolveDestroyVolumes();
      dmsComposeManager.destroy(result.get(), resolvedDestroyVolumes);
      t.printStatusLn("Finished cluster destruction");
      return 0;
    }

    t.printErrorLn("Could not find DMS configuration: %s", dmsConfigStore.getDmsConfigFilePath());
    return 1;
  }

  private boolean resolveDestroyVolumes() {
    boolean askQuestion = !force && destroyVolumes;
    boolean resolvedDestroyVolumes = force && destroyVolumes;
    if (askQuestion) {
      resolvedDestroyVolumes =
          questionFactory
              .newSingleQuestion(
                  WARNING,
                  Boolean.class,
                  "Are you sure you want to destroy the volumes for all services? This is IRREVERSIBLE ",
                  true,
                  false)
              .getAnswer();
      if (!resolvedDestroyVolumes) {
        t.printStatus("Volumes will NOT be destroyed!");
      }
    }
    if (resolvedDestroyVolumes) {
      t.printStatus("Forcefully destroying all volumes!");
    }
    return resolvedDestroyVolumes;
  }
}
