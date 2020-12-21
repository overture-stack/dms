package bio.overture.dms.cli.command.cluster;

import static bio.overture.dms.cli.model.enums.Constants.CONFIG_FILE_NAME;
import static bio.overture.dms.core.util.FileUtils.checkFileExists;

import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.util.VersionProvider;
import bio.overture.dms.compose.service.ComposeStackGraphGenerator;
import bio.overture.dms.compose.service.ComposeStackManager;
import bio.overture.dms.compose.service.ComposeStackRenderEngine;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.swarm.service.SwarmService;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "apply",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Deploy a configuration to the cluster")
public class ClusterApplyCommand implements Callable<Integer> {

  private final SwarmService swarmService;
  private final Terminal terminal;
  private final ComposeStackRenderEngine composeStackRenderEngine;
  private final ObjectSerializer yamlSerializer;

  @Autowired
  public ClusterApplyCommand(
      @NonNull SwarmService swarmService,
      @NonNull Terminal terminal,
      @NonNull ComposeStackRenderEngine composeStackRenderEngine,
      @NonNull ObjectSerializer yamlSerializer) {
    this.swarmService = swarmService;
    this.terminal = terminal;
    this.composeStackRenderEngine = composeStackRenderEngine;
    this.yamlSerializer = yamlSerializer;
  }

  @Override
  public Integer call() throws Exception {
    val volumeName = "robvolume";
    val networkName = "robnetwork";
    val configFile =
        Paths.get(System.getProperty("user.home")).resolve(".dms").resolve(CONFIG_FILE_NAME);
    checkFileExists(configFile);
    val dmsConfig = yamlSerializer.deserializeFile(configFile.toFile(), DmsConfig.class);

    val cs = composeStackRenderEngine.render(dmsConfig);

    val executor = Executors.newFixedThreadPool(4);
    val generator = new ComposeStackGraphGenerator(networkName, volumeName, swarmService);
    val manager = new ComposeStackManager(executor, generator, swarmService);
    terminal.printStatusLn("Starting deployment...");
    manager.deploy(cs);
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);
    terminal.printStatusLn("Deployment completed successfully");
    return 0;
  }

  // TODO: template for what the deployer class should do.
  //  The cli should simply call this method. Also makes testing easier.
  //  The Command class is used to make sure the params are correctly mapped to the underlying
  // method.
  //  The underlying method is used to test the terminal output and interactive input.

  //  @RequiredArgsConstructor
  //  public static class Deployer<
  //      S extends DMSSpec<E>, E extends EGOSpec, C extends ComposeObject<I>, I extends
  // ComposeItem> {
  //
  //    private final SpecPersistence<S> specPersistence;
  //    private final ComposeRenderEngine<S, C> composeRendererEngine;
  //    private final bio.overture.dms.domain.ComposeManager<C> composeManager;
  //    // TODO    private final TextTerminal
  //
  //    public Integer call() throws Exception {
  //      val spec = specPersistence.readFromHome();
  //      val composeObject = composeRendererEngine.render(spec);
  //      composeManager.deploy(composeObject);
  //      return 0;
  //    }
  //  }
}
