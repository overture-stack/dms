package bio.overture.dms.infra.service;

import bio.overture.dms.infra.converter.EgoSpecConverter;
import bio.overture.dms.infra.docker.DockerService;
import bio.overture.dms.infra.docker.model.DockerContainer;
import bio.overture.dms.infra.spec.DmsSpec;
import bio.overture.dms.infra.spec.EgoSpec;
import com.github.dockerjava.api.command.InspectContainerResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class EgoDeploymentService {

  @NonNull private final DockerService dockerService;
  @NonNull private final EgoSpecConverter egoSpecConverter;

  public void deployEgo(@NonNull EgoSpec egoSpec){
    val egoDeployProperties = egoSpecConverter.convertSpec(egoSpec);
    deployDockerContainer(egoDeployProperties.getEgoDbDockerContainer(), true);
    deployDockerContainer(egoDeployProperties.getEgoApiDockerContainer(), true);

  }

  private String deployDockerContainer(DockerContainer<?> dockerContainer, boolean waitTillHealthy){
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
    dockerService.startContainer(containerId);
    // check if container already exists
    // check if container is running in specified network. if exists and healthy, then dont deploy. If DNE, then continue
    // create container
    // connect container to network
    // copy boot strap files to container
    // do any volume mounting
    // start container
    return containerId;
  }

}
