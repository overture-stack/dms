package bio.overture.dms.cli.command.cluster;

import static bio.overture.dms.util.FileUtils.checkFileExists;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

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
import lombok.NonNull;
import lombok.val;
import org.beryx.textio.TextTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(
    name = "destroy",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Destroy the cluster")
public class ClusterDestroyCommand implements Callable<Integer> {

  private final ObjectSerializer yamlSerializer;
  private final ComposeTemplateEngine composeTemplateEngine;
  private final DockerService dockerService;
  private final TextTerminal<?> textTerminal;

  @Option(
      names = {"-v", "--volumes"},
      required = false,
      showDefaultValue = ALWAYS,
      description = "Additionally destroy volumes")
  private boolean destroyVolumes = false;

  @Option(
      names = {"-f", "--force"},
      required = false,
      showDefaultValue = ALWAYS,
      description = "Forcefully destroy")
  private boolean force = false;

  @Autowired
  public ClusterDestroyCommand(
      @NonNull ObjectSerializer yamlSerializer,
      @NonNull ComposeTemplateEngine composeTemplateEngine,
      @NonNull DockerService dockerService,
      @NonNull TextTerminal<?> textTerminal) {
    this.yamlSerializer = yamlSerializer;
    this.composeTemplateEngine = composeTemplateEngine;
    this.dockerService = dockerService;
    this.textTerminal = textTerminal;
  }

  @Override
  public Integer call() throws Exception {
    val volumeName = "robvolume";
    val networkName = "robnetwork";
    val specFile = Paths.get(System.getProperty("user.home")).resolve(".dms").resolve("spec.yaml");
    checkFileExists(specFile);
    val dmsSpec = yamlSerializer.deserializeFile(specFile.toFile(), DmsSpec.class);

    val dc = composeTemplateEngine.render(dmsSpec);
    val generator = new ComposeGraphGenerator(networkName, volumeName, dockerService);
    val executor = Executors.newFixedThreadPool(4);
    val dockerComposer = new ComposeManager(executor, generator, dockerService);

    textTerminal.executeWithPropertiesPrefix(
        "status", x -> x.println("Starting cluster destruction: force=" + force));
    dockerComposer.destroy(dc, force, destroyVolumes);
    textTerminal.executeWithPropertiesPrefix(
        "status", x -> x.println("Finished cluster destruction"));
    return 0;
  }
}
