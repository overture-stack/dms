package bio.overture.dms.cli.command;

import java.util.concurrent.Callable;
import lombok.Data;

// TODO: implement bootstrap command
/**
 * Point of this class, is to bootstrap the loading of the spring application and main cli. This CLI
 * has no ties to spring, so it must be called programmatically. The values from this class is used
 * to set the profile, and various other spring related configuration.
 *
 * <p>For instance, by setting the profile here, the correct implementations can be loaded in
 * spring. Refer to score-client as an example
 */
@Data
public class BootstrapCommand implements Callable<Integer> {

  // TODO: By default, this is the number of threads available to the machine,
  // but optionally can be something smaller. This would then configure spring with the right amount
  // of threads;
  private int numThreads;

  @Override
  public Integer call() throws Exception {
    return null;
  }
}
