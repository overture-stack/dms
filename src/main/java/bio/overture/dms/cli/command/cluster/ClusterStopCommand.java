package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.DmsConfigStore;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.util.VersionProvider;
import java.util.concurrent.Callable;

import bio.overture.dms.compose.deployment.DmsComposeManager;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "stop",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Stop a running cluster, without deleting data volumes")
public class ClusterStopCommand implements Callable<Integer> {

  /** Dependencies */
  private final Terminal t;

  private final DmsComposeManager dmsComposeManager;
  private final DmsConfigStore dmsConfigStore;

  @Builder
  @Autowired
  public ClusterStopCommand(
      @NonNull Terminal terminal,
      @NonNull DmsComposeManager dmsComposeManager,
      @NonNull DmsConfigStore dmsConfigStore) {
    this.t = terminal;
    this.dmsComposeManager = dmsComposeManager;
    this.dmsConfigStore = dmsConfigStore;
  }

  @Override
  public Integer call() throws Exception {
    val result = dmsConfigStore.findStoredConfig();
    if (result.isPresent()) {
      t.printStatusLn(
          "Stopping cluster..");
      dmsComposeManager.destroy(result.get(), false);
      t.printStatusLn("Finished stopping cluster");
      return 0;
    }
    t.printErrorLn("Could not find DMS configuration: %s", dmsConfigStore.getDmsConfigFilePath());
    return 1;
  }
}
