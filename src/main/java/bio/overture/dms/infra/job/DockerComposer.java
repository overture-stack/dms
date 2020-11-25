package bio.overture.dms.infra.job;

import static bio.overture.dms.core.util.CollectionUtils.mapToUnmodifiableSet;
import static bio.overture.dms.infra.graph.ConcurrentGraphTraversal.createConcurrentGraphTraversal;

import bio.overture.dms.infra.docker.DCGraphGenerator;
import bio.overture.dms.infra.docker.DockerService;
import bio.overture.dms.infra.model.DCService;
import bio.overture.dms.infra.model.DockerCompose;
import java.util.concurrent.ExecutorService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor
public class DockerComposer {

  @NonNull private final ExecutorService executorService;
  @NonNull private final DCGraphGenerator graphGenerator;
  @NonNull private final DockerService dockerService;

  @SneakyThrows
  public void deploy(@NonNull DockerCompose dc) {
    val graph = graphGenerator.generateGraph(dc);
    createConcurrentGraphTraversal(executorService, graph)
        .traverse(x -> x.getData().run(), () -> {});
  }

  @SneakyThrows
  public void destroy(@NonNull DockerCompose dc, boolean force, boolean removeVolumes) {
    dockerService.deleteContainersByName(
        executorService,
        mapToUnmodifiableSet(dc.getServices(), DCService::getServiceName),
        force,
        removeVolumes);
  }
}
