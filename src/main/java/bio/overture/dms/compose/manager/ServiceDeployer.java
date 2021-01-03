package bio.overture.dms.compose.manager;

import bio.overture.dms.compose.model.stack.ComposeService;
import bio.overture.dms.swarm.service.SwarmService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

/**
 * Deploys a ComposeService, as well as running pre and post deployment tasks.
 * These tasks are statically defined at boot time, and are also idempotent.
 * If a Pre task exists for the input ComposeService, then it is run prior to
 * deploying the service. Similarly for the Post task.
 */
@Slf4j
@RequiredArgsConstructor(access = PRIVATE)
public class ServiceDeployer {

  @NonNull private final SwarmService swarmService;
  @NonNull private final Map<String, Runnable> preDeployTaskIndex;
  @NonNull private final Map<String, Runnable> postDeployTaskIndex;

  public void process(@NonNull ComposeService s, @NonNull Consumer<String> printCallback){
    preDeployTask(s);
    printMessage(printCallback, "Completed Pre Deployment task for service %s", s.getName());

    deploySwarmService(s);
    printMessage(printCallback, "Completed Service Deployment task for service %s", s.getName());

    postDeployTask(s);
    printMessage(printCallback, "Completed Post Deployment task for service %s", s.getName());
  }

  private void preDeployTask(ComposeService s){
    if (preDeployTaskIndex.containsKey(s.getName())){
      preDeployTaskIndex.get(s.getName()).run();
    }
  }

  private void postDeployTask(ComposeService s){
    if (postDeployTaskIndex.containsKey(s.getName())){
      postDeployTaskIndex.get(s.getName()).run();
    }
  }

  private static void printMessage(@NonNull Consumer<String> printCallback, @NonNull String formattedMessage, Object...args){
    printCallback.accept(format(formattedMessage, args));
  }

  private void deploySwarmService(ComposeService s) {
    swarmService.ping();
    val result = swarmService.findSwarmServiceInfo(s.getName(), true);
    if (result.isPresent()) {
      log.debug("Found service '{}' info, updating existing service spec", s.getName());
      val info = result.get();
      swarmService.updateSwarmService(info.getId(), s.getServerSpec(), info.getVersion());
    } else {
      log.debug("Service '{}' info was NOT found, create new service spec", s.getName());
      swarmService.createSwarmService(s.getServerSpec());
    }
  }

  public static ServiceDeployerBuilder builder(){
    return new ServiceDeployerBuilder();
  }

  public static class ServiceDeployerBuilder {

    private SwarmService swarmService;
    private Map<String, Runnable> preDeployTaskIndex = new HashMap<>();
    private Map<String, Runnable> postDeployTaskIndex = new HashMap<>();

    public ServiceDeployerBuilder swarmService(SwarmService swarmService) {
      this.swarmService = swarmService;
      return this;
    }

    public ServiceDeployerBuilder addPreDeployTask(@NonNull String serviceName, @NonNull Runnable r){
      this.preDeployTaskIndex.put(serviceName, r);
      return this;
    }

    public ServiceDeployerBuilder addPostDeployTask(@NonNull String serviceName, @NonNull Runnable r){
      this.postDeployTaskIndex.put(serviceName, r);
      return this;
    }

    public ServiceDeployer build() {
      return new ServiceDeployer(swarmService, preDeployTaskIndex, postDeployTaskIndex);
    }
  }
}
