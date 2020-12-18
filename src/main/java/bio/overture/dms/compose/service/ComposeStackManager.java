package bio.overture.dms.compose.service;

import static bio.overture.dms.core.util.CollectionUtils.mapToUnmodifiableList;
import static bio.overture.dms.graph.ConcurrentGraphTraversal.createConcurrentGraphTraversal;

import bio.overture.dms.compose.model.stack.ComposeService;
import bio.overture.dms.compose.model.stack.ComposeStack;
import bio.overture.dms.swarm.service.SwarmService;
import java.util.List;
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

  @SneakyThrows
  public void destroy(@NonNull ComposeStack cs, boolean destroyVolumes) {
    val serviceNames = mapToUnmodifiableList(cs.getServices(), ComposeService::getName);
    destroy(serviceNames, destroyVolumes);
  }

  public void destroy(@NonNull List<String> serviceNames, boolean destroyVolumes) {
    swarmService.deleteServices(serviceNames, destroyVolumes);
  }
}
