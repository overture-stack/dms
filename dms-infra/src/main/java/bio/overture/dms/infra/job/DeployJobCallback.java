package bio.overture.dms.infra.job;

import bio.overture.dms.infra.graph.Graph;
import bio.overture.dms.infra.graph.Node;
import bio.overture.dms.infra.util.Concurrency;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@Slf4j
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

  private void trySubmit(Runnable r){
    Concurrency.trySubmit(executorService, r);
  }

  private void asyncRunDeployJob(DeployJob j){
    executorService.submit(() -> j.start(this));
  }

  @Override
  @SneakyThrows
  public void run() {
    graph.getRoots().stream()
        .map(Node::getData)
        .forEach(this::asyncRunDeployJob);
    countDownLatch.await(1, TimeUnit.HOURS);
  }

  @Override
  public void onDone(@NonNull DeployJob job) {
    countDownLatch.countDown();
    graph.findNodeByName(job.getName()).ifPresent(this::processCurrentNode);
  }

  @Override
  public void onError(DeployJob job) {
    log.error("ERROR occurred, so counting down latch");
    countDownLatch.countDown();
  }

  private void processCurrentNode(Node<DeployJob> parentNode){
    for (val childNode : graph.getChildNodes(parentNode)) {
      val currentChildDepCount = childNode.decrementUnvisitedParents();
      if (currentChildDepCount == 0) {
        asyncProcessChildNode(childNode);
      }
    }
  }

  private void asyncProcessChildNode(Node<DeployJob> childNode){
    trySubmit(() -> syncProcessChildNode(childNode));
  }

  private void syncProcessChildNode(Node<DeployJob> childNode){
    childNode.getData().start(this);
  }


}
