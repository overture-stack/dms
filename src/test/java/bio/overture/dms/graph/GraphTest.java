package bio.overture.dms.graph;

import static bio.overture.dms.graph.ConcurrentGraphTraversal.createConcurrentGraphTraversal;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.overture.dms.compose.model.job.ComposeJob;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class GraphTest {

  private Map<String, Integer> idx;
  private int order;

  @BeforeEach
  public void beforeTest() {
    this.idx = new HashMap<>();
    this.order = 0;
  }

  @Test
  @SneakyThrows
  public void testConcurrentGraphTraversal() {
    val executor = newFixedThreadPool(10);

    // Create nodes
    val a = createTestJob("a");
    val b = createTestJob("b");
    val c = createTestJob("c");
    val d = createTestJob("d");
    val e = createTestJob("e");
    val f = createTestJob("f");
    val g = createTestJob("g");
    val h = createTestJob("h");

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

    /** Assert the jobs are executed in the correct order */
    // This is read as "b" and "c" are completed AFTER "a"
    assertJobCompletedAfter("a", "b", "c");
    assertJobCompletedAfter("b", "g", "e", "f");
    assertJobCompletedAfter("c", "d");
    assertJobCompletedAfter("e", "h");
    assertJobCompletedAfter("f", "h");
    // This is read as "e" and "f" are completed BEFORE "h"
    assertJobCompletedBefore("h", "e", "f");
  }

  private void assertJobCompletedAfter(String jobName, String... afterJobNames) {
    assertJobCompleted(false, jobName, afterJobNames);
  }

  private void assertJobCompletedBefore(String jobName, String... beforeJobNames) {
    assertJobCompleted(true, jobName, beforeJobNames);
  }

  private void assertJobCompleted(boolean isBefore, String jobName, String... otherJobNames) {
    stream(otherJobNames)
        .forEach(
            other -> {
              val result = (isBefore ? 1 : -1) * Integer.compare(idx.get(jobName), idx.get(other));
              assertTrue(
                  result < 0,
                  format(
                      "The job %s[%s] did not finish %s %s[%s]",
                      jobName,
                      idx.get(jobName),
                      isBefore ? "before" : "after",
                      other,
                      idx.get(other)));
            });
  }

  private synchronized void addJob(String name) {
    idx.put(name, order++);
  }

  private ComposeJob createTestJob(final String name) {
    return ComposeJob.builder()
        .name(name)
        .deployTask(
            () -> {
              log.info("This is '{}'", name);
              addJob(name);
            })
        .build();
  }
}
