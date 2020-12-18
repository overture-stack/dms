package bio.overture.dms.compose.docker;

import static bio.overture.dms.graph.ConcurrentGraphTraversal.createConcurrentGraphTraversal;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.overture.dms.compose.model.job.ComposeJob;
import bio.overture.dms.graph.MemoryGraph;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

@Slf4j
public class RobTest {

  @Test
  @SneakyThrows
  public void testExampleGraph() {
    val executor = Executors.newFixedThreadPool(4);

    // Create nodes
    val a = createTestJob("a", 100);
    val b = createTestJob("b", 100);
    val c = createTestJob("c", 100);
    val d = createTestJob("d", 100);
    val e = createTestJob("e", 100);
    val f = createTestJob("f", 100);
    val g = createTestJob("g", 100);
    val h = createTestJob("h", 100);

    // Build graph
    val graph =
        MemoryGraph.<ComposeJob>builder()
            .addEdge(c, a)
            .addEdge(d, c)
            .addEdge(h, e)
            .addEdge(h, f)
            .addEdge(g, b)
            .addEdge(e, b)
            .addEdge(f, b)
            .addEdge(b, a)
            .build();

    // Create Graph Traverser
    val graphTraverser = createConcurrentGraphTraversal(executor, graph);

    assertTrue(graph.findNodeByName("a").isPresent());
    assertTrue(graph.findNodeByName("b").isPresent());
    assertTrue(graph.findNodeByName("c").isPresent());
    assertTrue(graph.findNodeByName("d").isPresent());
    assertTrue(graph.findNodeByName("e").isPresent());
    assertTrue(graph.findNodeByName("f").isPresent());
    assertTrue(graph.findNodeByName("g").isPresent());
    assertTrue(graph.findNodeByName("h").isPresent());

    val a1 = graph.findNodeByName("a").get();
    val b1 = graph.findNodeByName("b").get();
    val c1 = graph.findNodeByName("c").get();
    val d1 = graph.findNodeByName("d").get();
    val e1 = graph.findNodeByName("e").get();
    val f1 = graph.findNodeByName("f").get();
    val g1 = graph.findNodeByName("g").get();
    val h1 = graph.findNodeByName("h").get();

    assertTrue(graph.getChildNodes(a).containsAll(Set.of()));
    assertTrue(graph.getChildNodes(b).containsAll(Set.of(a1)));
    assertTrue(graph.getChildNodes(c).containsAll(Set.of(a1)));
    assertTrue(graph.getChildNodes(d).containsAll(Set.of(c1)));
    assertTrue(graph.getChildNodes(e).containsAll(Set.of(b1)));
    assertTrue(graph.getChildNodes(f).containsAll(Set.of(b1)));
    assertTrue(graph.getChildNodes(g).containsAll(Set.of(b1)));
    assertTrue(graph.getChildNodes(h).containsAll(Set.of(e1, f1)));

    val start = System.currentTimeMillis();
    graphTraverser.traverse(x -> x.getData().run(), () -> {});
    val diff = System.currentTimeMillis() - start;
    log.info("Diff: {} ms", diff);

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);
  }

  private static ComposeJob createTestJob(String name, long delayMs) {
    return ComposeJob.builder()
        .name(name)
        .deployTask(
            () -> {
              try {
                Thread.sleep(delayMs);
                log.info("This is '{}' with delay {}", name, delayMs);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            })
        .build();
  }
}
