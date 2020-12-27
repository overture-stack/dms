package bio.overture.dms.cli.command.config;

import bio.overture.dms.cli.DmsConfigStore;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.util.VersionProvider;
import java.util.concurrent.Callable;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "get",
    aliases = {"g"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Get the current configuration")
public class ConfigGetCommand implements Callable<Integer> {

  private final DmsConfigStore dmsConfigStore;
  private final Terminal t;

  @Autowired
  public ConfigGetCommand(@NonNull DmsConfigStore dmsConfigStore, @NonNull Terminal terminal) {
    this.dmsConfigStore = dmsConfigStore;
    this.t = terminal;
  }

  @Override
  public Integer call() throws Exception {
    val result = dmsConfigStore.findStoredConfigContents();
    if (result.isPresent()) {
      t.println(result.get());
      return 0;
    }
    t.printError("The dms configuration '%s' does not exist", dmsConfigStore.getDmsConfigFilePath());
    return 1;
  }
}
