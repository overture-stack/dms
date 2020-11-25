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
}
