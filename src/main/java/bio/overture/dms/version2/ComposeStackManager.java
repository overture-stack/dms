package bio.overture.dms.version2;

import static bio.overture.dms.graph.ConcurrentGraphTraversal.createConcurrentGraphTraversal;
import static bio.overture.dms.util.CollectionUtils.mapToUnmodifiableList;

import bio.overture.dms.version2.model.ComposeService2;
import bio.overture.dms.version2.model.ComposeStack;
import java.util.concurrent.ExecutorService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor
public class ComposeStackManager {

  @NonNull private final ExecutorService executorService;
  @NonNull private final ComposeStackGraphGenerator graphGenerator;
  @NonNull private final SwarmService swarmService;

  @SneakyThrows
  public void deploy(@NonNull ComposeStack cs) {
    val graph = graphGenerator.generateGraph(cs);
    createConcurrentGraphTraversal(executorService, graph)
        .traverse(x -> x.getData().run(), () -> {});
  }

  // TODO: add force and removeVolumes switches. These are avaialble when managing containers
  // outside of a swarm,
  // however might not be needed when managing a swarm. Volumes might have to be managed manually.
  @SneakyThrows
  public void destroy(@NonNull ComposeStack cs) {
    swarmService.deleteSwarmServices(
        mapToUnmodifiableList(cs.getServices(), ComposeService2::getName));
  }
}
