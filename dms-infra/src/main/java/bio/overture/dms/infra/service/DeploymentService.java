package bio.overture.dms.infra.service;

import bio.overture.dms.infra.docker.DockerService;
import bio.overture.dms.infra.docker.model.DockerContainer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.concurrent.Executors.newFixedThreadPool;

@Slf4j
@RequiredArgsConstructor
public class DeploymentService {

  /**
   * Constants
   */
  private static final DeployCallback EMPTY_DEPLOY_CALLBACK = new EmptyDeployCallback();

  /**
   * Dependencies
   */
  @NonNull private final DockerService dockerService;
  @NonNull private final ExecutorService executorService;



  public Future<String> defaultDeploy(@NonNull DockerContainer<?> dockerContainer){
    return deploy(dockerContainer, EMPTY_DEPLOY_CALLBACK);
  }

  public Future<String> deploy(@NonNull DockerContainer<?> dockerContainer, @NonNull DeployCallback callback){
    return executorService.submit(() -> {
      try {
        return deployDockerContainer(dockerContainer, callback);
      } catch (Exception e){
        log.info("Deployment failed: {}", e.getMessage());
        throw e;
      }
    });
  }

  public static DeploymentService createDefaultDeploymentService(@NonNull DockerService dockerService, int numFixedThreads) {
    checkArgument(numFixedThreads > 0, "The number of fixed thread must be greater than 0");
    return createDeploymentService(dockerService,newFixedThreadPool(numFixedThreads));
  }

  public static DeploymentService createDeploymentService(@NonNull DockerService dockerService, @NonNull ExecutorService executorService) {
    return new DeploymentService(dockerService, executorService);
  }

  private String deployDockerContainer(DockerContainer<?> dockerContainer, DeployCallback callback){
    dockerService.ping();
    dockerService.pullImage(dockerContainer.getDockerImage());
    val result = dockerService.findContainerId(dockerContainer.getName());
    if (result.isPresent()){
      val containerId = result.get();
      if (dockerService.isContainerRunning(containerId)){
        return containerId;
      } else {
        dockerService.deleteContainer(containerId, false, false);
      }
    }

    val containerId = dockerService.createContainer(dockerContainer);
    callback.onCreate(containerId);

    dockerService.startContainer(containerId);
    callback.onStart(containerId);
    return containerId;
  }

}
