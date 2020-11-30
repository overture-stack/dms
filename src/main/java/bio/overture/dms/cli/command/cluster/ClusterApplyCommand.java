package bio.overture.dms.cli.command.cluster;

import static bio.overture.dms.util.FileUtils.checkFileExists;

import bio.overture.dms.cli.util.VersionProvider;
import bio.overture.dms.compose.ComposeGraphGenerator;
import bio.overture.dms.compose.ComposeManager;
import bio.overture.dms.compose.ComposeTemplateEngine;
import bio.overture.dms.docker.DockerService;
import bio.overture.dms.model.spec.DmsSpec;
import bio.overture.dms.util.ObjectSerializer;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.val;
import org.beryx.textio.TextTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "apply",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Deploy a spec to the cluster")
public class ClusterApplyCommand implements Callable<Integer> {

  private final DockerService dockerService;
  private final TextTerminal<?> textTerminal;
  private final ComposeTemplateEngine composeTemplateEngine;
  private final ObjectSerializer yamlSerializer;

  @Autowired
  public ClusterApplyCommand(
      @NonNull DockerService dockerService,
      @NonNull TextTerminal<?> textTerminal,
      @NonNull ComposeTemplateEngine composeTemplateEngine,
      @NonNull ObjectSerializer yamlSerializer) {
    this.dockerService = dockerService;
    this.textTerminal = textTerminal;
    this.composeTemplateEngine = composeTemplateEngine;
    this.yamlSerializer = yamlSerializer;
  }

  @Override
  public Integer call() throws Exception {
    val volumeName = "robvolume";
    val networkName = "robnetwork";
    val specFile = Paths.get(System.getProperty("user.home")).resolve(".dms").resolve("spec.yaml");
    checkFileExists(specFile);
    val dmsSpec = yamlSerializer.deserializeFile(specFile.toFile(), DmsSpec.class);

    val dc = composeTemplateEngine.render(dmsSpec);

    val executor = Executors.newFixedThreadPool(4);
    val generator = new ComposeGraphGenerator(networkName, volumeName, dockerService);
    val dockerComposer = new ComposeManager(executor, generator, dockerService);
    textTerminal.executeWithPropertiesPrefix("status", x -> x.println("Starting deployement..."));
    dockerComposer.deploy(dc);
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);
    textTerminal.executeWithPropertiesPrefix(
        "status", x -> x.println("Deployment completed successfully"));
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
