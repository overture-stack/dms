package bio.overture.dms.graph;

import static bio.overture.dms.core.util.Concurrency.trySubmit;
import static bio.overture.dms.core.util.Exceptions.checkState;

import bio.overture.dms.core.model.Nameable;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

public class ConcurrentGraphTraversal<T extends Nameable> {

  /** Dependencies */
  private final ExecutorService executorService;

  private final MemoryGraph<T> graph;

  /** State */
  private CountDownLatch nodeCountDownLatch;

  private ConcurrentGraphTraversal(
      @NonNull ExecutorService executorService, @NonNull MemoryGraph<T> graph) {
    this.executorService = executorService;
    this.graph = graph;
    reset();
  }

  public void traverse(Consumer<Node<T>> visitFunction, Runnable errorCallback) {
    checkState(
        nodeCountDownLatch.getCount() > 0,
        "The graph is either empty or has already been traversed. Please reset.");
    new FunctionalConcurrentGraphTraversal(visitFunction, errorCallback).traverse();
  }

  public synchronized void reset() {
    nodeCountDownLatch = new CountDownLatch(graph.numNodes());
    graph.reset();
  }

  public static <T extends Nameable> ConcurrentGraphTraversal<T> createConcurrentGraphTraversal(
      @NonNull ExecutorService executorService, @NonNull MemoryGraph<T> graph) {
    return new ConcurrentGraphTraversal<T>(executorService, graph);
  }

  @RequiredArgsConstructor
  private class FunctionalConcurrentGraphTraversal {

    @NonNull private final Consumer<Node<T>> visitFunction;
    @NonNull private final Runnable errorCallback;

    @SneakyThrows
    public void traverse() {
      graph.getRoots().forEach(this::asyncVisitCurrent);
      nodeCountDownLatch.await(1, TimeUnit.HOURS);
    }

    private void asyncVisitCurrent(Node<T> node) {
      trySubmit(
          executorService,
          () -> {
            syncVisitCurrent(node);
            asyncVisitChildren(node);
          },
          () -> {
            errorCallback.run();
            nodeCountDownLatch.countDown();
          });
    }

    private void syncVisitCurrent(Node<T> node) {
      visitFunction.accept(node);
      nodeCountDownLatch.countDown();
    }

    private void asyncVisitChildren(Node<T> node) {
      graph
          .findNodeByName(node.getData().getName())
          .map(graph::getChildNodes)
          .map(Collection::stream)
          .ifPresent(childNodeStream -> childNodeStream.forEach(this::asyncVisitChild));
    }

    private void asyncVisitChild(Node<T> childNode) {
      val currentChildDepCount = childNode.decrementUnvisitedParents();
      if (currentChildDepCount == 0) {
        asyncVisitCurrent(childNode);
      }
    }
  }
}
