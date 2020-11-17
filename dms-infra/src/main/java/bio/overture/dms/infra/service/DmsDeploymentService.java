package bio.overture.dms.infra.service;

import bio.overture.dms.infra.converter.EgoContainerConverter;
import bio.overture.dms.infra.docker.DockerService;
import bio.overture.dms.infra.docker.model.DockerContainer;
import bio.overture.dms.infra.spec.DmsSpec;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static bio.overture.dms.core.Futures.getFuture;
import static bio.overture.dms.core.Futures.getFutures;
import static bio.overture.dms.core.Futures.waitForFutures;
import static bio.overture.dms.core.Joiner.COMMA;
import static java.util.stream.Collectors.toUnmodifiableList;

@Slf4j
@RequiredArgsConstructor
public class DmsDeploymentService {

  @NonNull private final ExecutorService executorService;
  @NonNull private final EgoContainerConverter egoContainerConverter;
  @NonNull private final DeploymentService deploymentService;
  @NonNull private final DockerService dockerService;

  public void deploy(@NonNull DmsSpec dmsSpec){
    // Generate all containers
    val egoDockerContainers = egoContainerConverter.convert(dmsSpec.getEgo());

    val futureEgoPulls = egoDockerContainers.stream()
        .map(DockerContainer::getDockerImage)
        .map(x -> executorService.submit(() -> {
          log.info("Starting docker image pull: {}", x);
          dockerService.pullImage(x);
          log.info("Finished docker image pull: {}", x);
        }))
        .collect(toUnmodifiableList());

    // Do other pulls concurrently here
    // ......

    // Continue with Ego deployment when ego pulls are done
    val egoDeployment =
        executorService.submit(
            () -> {
              log.info("Waiting for Ego deployment docker images to be pulled");
              waitForFutures(futureEgoPulls);
              log.info("Finished pull all ego deployment docker images");
              final List<Future<String>> futureEgoContainerIds =
                  egoDockerContainers.stream()
                      .map(deploymentService::defaultDeploy)
                      .collect(toUnmodifiableList());
              final String names = COMMA.join(egoDockerContainers.stream()
                  .map( DockerContainer::getName));
              log.info("Waiting for Ego containers [{}] to be deployed", names);
              final List<String> out = getFutures(futureEgoContainerIds);
              log.info("Finished deploying Ego containers [{}]", names);
              return out;
            });

    //Wait for ego deployement
    log.info("Waiting for ego deployment");
    getFuture(egoDeployment);
    log.info("Finished deploying EGO");
  }


}
