package bio.overture.dms.compose.manager;

import bio.overture.dms.compose.model.ComposeServiceResources;
import bio.overture.dms.core.Messenger;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.swarm.service.SwarmService;
import com.github.dockerjava.api.model.ServiceSpec;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static bio.overture.dms.compose.manager.ServiceDeployer.DeployTypes.CREATE;
import static bio.overture.dms.compose.manager.ServiceDeployer.DeployTypes.UPDATE;
import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_DB;
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

  /**
   * Constants
   */
  private static final int NUM_RETRIES = 300;
  private static final Duration POLL_PERIOD = Duration.ofSeconds(2);

  /**
   * Dependencies
   */
  private final SwarmService swarmService;
  private final Messenger messenger;
  private final ServiceSpecRenderEngine serviceSpecRenderEngine;

  @Autowired
  public ServiceDeployer(@NonNull SwarmService swarmService,
      @NonNull Messenger messenger,
      @NonNull ServiceSpecRenderEngine serviceSpecRenderEngine) {
    this.swarmService = swarmService;
    this.messenger = messenger;
    this.serviceSpecRenderEngine = serviceSpecRenderEngine;
  }

  public DeployTypes deploy(@NonNull ServiceSpec s){
    val out = deploySwarmService(s);
    messenger.send("Completed deployment task for service %s", s.getName());
    return out;
  }

  public DeployTypes deployAndWait(@NonNull DmsConfig dmsConfig, @NonNull ComposeServiceResources composeServiceResource){
    val serviceSpec = serviceSpecRenderEngine.render(dmsConfig, composeServiceResource)
        .orElseThrow();
    val deployType = deploy(serviceSpec);
    waitForServiceRunning(serviceSpec);
    return deployType;
  }

  //TODO: not working properly. Is not waiting for service to be RUNNING. Specifically, ego-db says "waiting" and then doesnt show "now running"
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
