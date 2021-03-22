package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.DmsConfigStore;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.util.VersionProvider;
import bio.overture.dms.compose.deployment.DmsComposeManager;
import java.util.concurrent.Callable;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "start",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Deploy a configuration to the cluster")
public class ClusterStartCommand implements Callable<Integer> {

  private final Terminal t;
  private final DmsComposeManager dmsComposeManager;
  private final DmsConfigStore dmsConfigStore;

  @Autowired
  public ClusterStartCommand(
      @NonNull Terminal terminal,
      @NonNull DmsComposeManager dmsComposeManager,
      @NonNull DmsConfigStore dmsConfigStore) {
    this.dmsComposeManager = dmsComposeManager;
    this.t = terminal;
    this.dmsConfigStore = dmsConfigStore;
  }

  @Override
  public Integer call() throws Exception {
    val result = dmsConfigStore.findStoredConfig();
    if (result.isPresent()) {
      t.printStatusLn("Starting deployment...");
      dmsComposeManager.deploy(result.get());
      t.printStatusLn("Deployment completed successfully");
      return 0;
    }

    t.printErrorLn("Could not find DMS configuration: %s", dmsConfigStore.getDmsConfigFilePath());
    return 1;
  }
}
