package bio.overture.dms.infra.docker;

import bio.overture.dms.infra.docker.model.DockerImage;
import bio.overture.dms.infra.job.DeployJob;
import bio.overture.dms.infra.job.DeployJobCallback;
import bio.overture.dms.infra.graph.Graph;
import bio.overture.dms.infra.graph.Node;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class RobTest {

  @Test
  public void testRob(){
    val d = DockerImage.builder()
        .accountName("sdf")
        .repositoryName("fffff")
        .tag(234+"")
        .build();
    log.info("Sadf");


  }

  @Test
  @SneakyThrows
  public void testExampleGraph(){
    val executor = Executors.newFixedThreadPool(4);

    // Create nodes
    val a = createTestJob("a",  100);
    val b = createTestJob("b",  100);
    val c = createTestJob("c",  100);
    val d = createTestJob("d",  100);
    val e = createTestJob("e",  100);
    val f = createTestJob("f",  100);
    val g = createTestJob("g",  100);
    val h = createTestJob("h",  100);

    // Build graph
    val graph= Graph.<DeployJob>builder()
        .addEdge(c, a)
        .addEdge(d, c)
        .addEdge(h, e)
        .addEdge(h, f)
        .addEdge(g, b)
        .addEdge(e, b)
        .addEdge(f, b)
        .addEdge(b, a)
        .build();

    // Create JobCallback
    val deployJobCallback = new DeployJobCallback(executor, graph);

    assertTrue(graph.getNodeByName("a").isPresent());
    assertTrue(graph.getNodeByName("b").isPresent());
    assertTrue(graph.getNodeByName("c").isPresent());
    assertTrue(graph.getNodeByName("d").isPresent());
    assertTrue(graph.getNodeByName("e").isPresent());
    assertTrue(graph.getNodeByName("f").isPresent());
    assertTrue(graph.getNodeByName("g").isPresent());
    assertTrue(graph.getNodeByName("h").isPresent());

    assertTrue(graph.getChildNodes(a).containsAll(Set.of()));
    assertTrue(graph.getChildNodes(b).containsAll(Set.of(a)));
    assertTrue(graph.getChildNodes(c).containsAll(Set.of(a)));
    assertTrue(graph.getChildNodes(d).containsAll(Set.of(c)));
    assertTrue(graph.getChildNodes(e).containsAll(Set.of(b)));
    assertTrue(graph.getChildNodes(f).containsAll(Set.of(b)));
    assertTrue(graph.getChildNodes(g).containsAll(Set.of(b)));
    assertTrue(graph.getChildNodes(h).containsAll(Set.of(e, f)));

    val start = System.currentTimeMillis();
    deployJobCallback.run();
    val diff = System.currentTimeMillis() - start;
    log.info("Diff: {} ms", diff);

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);
  }

  private static Node<DeployJob> createTestJob(String name,long delayMs){
    return Node.<DeployJob>builder()
        .data(
            DeployJob.builder()
                .name(name)
                .deployTask(() -> {
                  try {
                    Thread.sleep(delayMs);
                    log.info("This is '{}' with delay {}", name, delayMs);
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                })
                .build())
        .build();
  }

}
