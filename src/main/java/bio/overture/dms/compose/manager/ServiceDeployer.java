package bio.overture.dms.compose.manager;

import bio.overture.dms.compose.model.stack.ComposeService;
import bio.overture.dms.core.Messenger;
import bio.overture.dms.swarm.service.SwarmService;
import com.github.dockerjava.api.model.ServiceSpec;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Consumer;

import static bio.overture.dms.compose.manager.ServiceDeployer.DeployTypes.CREATE;
import static bio.overture.dms.compose.manager.ServiceDeployer.DeployTypes.UPDATE;
import static java.lang.String.format;

/**
 * Deploys a ComposeService, as well as running pre and post deployment tasks.
 * These tasks are statically defined at boot time, and are also idempotent.
 * If a Pre task exists for the input ComposeService, then it is run prior to
 * deploying the service. Similarly for the Post task.
 */
@Slf4j
@Component
public class ServiceDeployer {

  private static final int NUM_RETRIES = 300;
  private static final Duration POLL_PERIOD = Duration.ofSeconds(2);
  private final SwarmService swarmService;
  private final Messenger messenger;

  @Autowired
  public ServiceDeployer(@NonNull SwarmService swarmService,
     @NonNull Messenger messenger) {
    this.swarmService = swarmService;
    this.messenger = messenger;
  }

  public DeployTypes process(@NonNull ServiceSpec s){
    val out = deploySwarmService(s);
    messenger.send("Completed deployment task for service %s", s.getName());
    return out;
  }

  //TODO: not working properly. Is not waiting for service to be RUNNING
  public void waitForServiceRunning(@NonNull ServiceSpec s){
    messenger.send("Waiting for the service '%s' to be in the RUNNING state", s.getName());
    swarmService.waitForServiceRunning(s.getName(), NUM_RETRIES, POLL_PERIOD);
  }

  private DeployTypes deploySwarmService(ServiceSpec s) {
    swarmService.ping();
    val result = swarmService.findSwarmServiceInfo(s.getName(), true);
    if (result.isPresent()) {
      log.debug("Found service '{}' info, updating existing service spec", s.getName());
      val info = result.get();
      swarmService.updateSwarmService(info.getId(), s, info.getVersion());
      return UPDATE;
    } else {
      log.debug("Service '{}' info was NOT found, create new service spec", s.getName());
      swarmService.createSwarmService(s);
      return CREATE;
    }
  }

  public enum DeployTypes{
    UPDATE,
    CREATE;
  }


}
