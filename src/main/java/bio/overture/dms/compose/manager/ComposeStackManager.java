package bio.overture.dms.compose.manager;

import static bio.overture.dms.core.util.CollectionUtils.mapToUnmodifiableList;
import static bio.overture.dms.graph.ConcurrentGraphTraversal.createConcurrentGraphTraversal;

import bio.overture.dms.compose.model.stack.ComposeService;
import bio.overture.dms.compose.model.stack.ComposeStack;
import bio.overture.dms.swarm.service.SwarmService;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

/** Implementation of a ComposeManager that manages a Swarm cluster using ComposeStack objects */
@Slf4j
@Component
public class ComposeStackManager implements ComposeManager<ComposeStack> {

  private final ExecutorService executorService;
  private final ComposeStackGraphGenerator graphGenerator;
  private final SwarmService swarmService;

  public ComposeStackManager(
      @NonNull ExecutorService executorService,
      @NonNull ComposeStackGraphGenerator graphGenerator,
      @NonNull SwarmService swarmService) {
    this.executorService = executorService;
    this.graphGenerator = graphGenerator;
    this.swarmService = swarmService;
  }

  @Override
  public void deploy(@NonNull ComposeStack cs) {
    swarmService.initializeSwarm();
    val graph = graphGenerator.generateGraph(cs);
    createConcurrentGraphTraversal(executorService, graph)
        .traverse(x -> x.getData().run(), () -> {});
  }

  @Override
  public void destroy(@NonNull ComposeStack cs, boolean destroyVolumes) {
    val serviceNames = mapToUnmodifiableList(cs.getServices(), ComposeService::getName);
    destroy(serviceNames, destroyVolumes);
  }

  private void destroy(@NonNull Collection<String> serviceNames, boolean destroyVolumes) {
    swarmService.initializeSwarm();
    swarmService.deleteServices(serviceNames, destroyVolumes);
  }
}
