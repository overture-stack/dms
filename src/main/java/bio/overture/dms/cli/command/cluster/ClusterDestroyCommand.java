package bio.overture.dms.cli.command.cluster;

import static bio.overture.dms.util.FileUtils.checkFileExists;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.util.VersionProvider;
import bio.overture.dms.model.spec.DmsSpec;
import bio.overture.dms.util.ObjectSerializer;
import bio.overture.dms.version2.ComposeStackGraphGenerator;
import bio.overture.dms.version2.ComposeStackManager;
import bio.overture.dms.version2.ComposeStackRenderEngine;
import bio.overture.dms.version2.SwarmService;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "destroy",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Destroy the cluster")
public class ClusterDestroyCommand implements Callable<Integer> {

  private final ObjectSerializer yamlSerializer;
  private final ComposeStackRenderEngine composeStackRenderEngine;
  private final SwarmService swarmService;
  private final QuestionFactory questionFactory;
  private final Terminal terminal;

//  @Option(
//      names = {"-v", "--volumes"},
//      required = false,
//      showDefaultValue = ALWAYS,
//      description = "Additionally destroy volumes")
  private boolean destroyVolumes = false;
  //
  //  @Option(
  //      names = {"-f", "--force"},
  //      required = false,
  //      showDefaultValue = ALWAYS,
  //      description = "Forcefully destroy volumes without asking first")
  private boolean force = false;

  @Autowired
  public ClusterDestroyCommand(
      @NonNull ObjectSerializer yamlSerializer,
      @NonNull ComposeStackRenderEngine composeStackRenderEngine,
      @NonNull SwarmService swarmService,
      @NonNull QuestionFactory questionFactory,
      @NonNull Terminal terminal) {
    this.yamlSerializer = yamlSerializer;
    this.composeStackRenderEngine = composeStackRenderEngine;
    this.swarmService = swarmService;
    this.terminal = terminal;
    this.questionFactory = questionFactory;
  }

  @Override
  public Integer call() throws Exception {
    val volumeName = "robvolume";
    val networkName = "robnetwork";
    val specFile = Paths.get(System.getProperty("user.home")).resolve(".dms").resolve("spec.yaml");
    checkFileExists(specFile);
    val dmsSpec = yamlSerializer.deserializeFile(specFile.toFile(), DmsSpec.class);

    val cs = composeStackRenderEngine.render(dmsSpec);
    val generator = new ComposeStackGraphGenerator(networkName, volumeName, swarmService);
    val executor = Executors.newFixedThreadPool(4);
    val stackManager = new ComposeStackManager(executor, generator, swarmService);

    terminal.printStatusLn(
        "Starting cluster destruction: force=%s  destroyVolumes=%s", force, destroyVolumes);
    val resolvedDestroyVolumes = resolveDestroyVolumes();
    stackManager.destroy(cs, resolvedDestroyVolumes);
    terminal.printStatusLn("Finished cluster destruction");
    return 0;
  }

  private boolean resolveDestroyVolumes() {
    boolean askQuestion = !force && destroyVolumes;
    boolean resolvedDestroyVolumes = force;
    if (askQuestion) {
      resolvedDestroyVolumes =
          questionFactory
              .newSingleQuestion(
                  "warning",
                  Boolean.class,
                  "Are you sure you want to destroy the volumes for all services? This is IRREVERSIBLE ",
                  true,
                  false)
              .getAnswer();
      if (!resolvedDestroyVolumes) {
        terminal.printStatus("Volumes will NOT be destroyed!");
      }
    }
    if (force) {
      terminal.printStatus("Forcefully destroying all volumes!");
    }
    return resolvedDestroyVolumes;
  }
}
