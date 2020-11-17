package bio.overture.dms.infra.job;

import bio.overture.dms.infra.graph.Graph;
import bio.overture.dms.infra.graph.Node;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class DeployJobCallback implements JobCallback, Runnable {

  @NonNull private final ExecutorService executorService;
  @NonNull private final Graph<DeployJob> graph;
  @NonNull private final CountDownLatch countDownLatch;

  public DeployJobCallback(@NonNull ExecutorService executorService,
      @NonNull Graph<DeployJob> graph) {
    this.executorService = executorService;
    this.graph = graph;
    this.countDownLatch = new CountDownLatch(graph.numNodes());
  }

  @Override
  @SneakyThrows
  public void run() {
    graph.getRoots().stream()
        .map(Node::getData)
        .forEach(x -> executorService.submit(() -> {
          x.start(this);
        }));
    countDownLatch.await(1, TimeUnit.HOURS);
  }

  @Override
  public void onDone(@NonNull DeployJob job) {
    countDownLatch.countDown();
    graph.getNodeByName(job.getName()).ifPresent(this::processParentNode);
  }

  private void processParentNode(Node<DeployJob> parentNode){
    for (val childNode : graph.getChildNodes(parentNode)) {
      val currentChildDepCount = childNode.decrDeps();
      if (currentChildDepCount == 0) {
        asyncProcessChildNode(childNode);
      }
    }
  }

  private void asyncProcessChildNode(Node<DeployJob> childNode){
    executorService.submit(() -> syncProcessChildNode(childNode));
  }

  private void syncProcessChildNode(Node<DeployJob> childNode){
    childNode.getData().start(this);
  }


}
