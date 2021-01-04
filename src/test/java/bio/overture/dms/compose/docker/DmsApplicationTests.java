package bio.overture.dms.compose.docker;

import static bio.overture.dms.cli.question.QuestionFactory.buildQuestionFactory;
import static bio.overture.dms.util.TestTextTerminal.createTestTextTerminal;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.overture.dms.cli.DmsConfigStore;
import bio.overture.dms.cli.command.cluster.ClusterApplyCommand;
import bio.overture.dms.cli.command.cluster.ClusterDestroyCommand;
import bio.overture.dms.cli.terminal.TerminalImpl;
import bio.overture.dms.compose.manager.DmsComposeManager;
import bio.overture.dms.compose.manager.DmsComposeManager2;
import bio.overture.dms.ego.EgoClientFactory;
import bio.overture.dms.util.TestTextTerminal;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Disabled
public class DmsApplicationTests {

  // NOTE: https://docs.docker.com/engine/swarm/how-swarm-mode-works/swarm-task-states/
  @Autowired DmsComposeManager dmsComposeManager;
  @Autowired DmsComposeManager2 dmsComposeManager2;
  @Autowired private DmsConfigStore dmsConfigStore;
  // TODO: add wiremocking tests for the ego client
  @Autowired private EgoClientFactory egoClientFactory;

  @Test
  void contextLoads() {}

  @Autowired private ClusterApplyCommand clusterApplyCommand;
  @Autowired private ClusterDestroyCommand clusterDestroyCommand;
  @Autowired private TestTextTerminal testTextTerminal;

  @Test
  @Disabled
  @SneakyThrows
  public void testDeploy() {
    val exitCode = clusterApplyCommand.call();
    assertEquals(0, exitCode);

    log.info(testTextTerminal.getOutputAndReset(false));
    log.info("Sdf");
  }

  @Test
  @Disabled
  @SneakyThrows
  public void testDestroy() {
    testTextTerminal.addInput("Y");
    clusterDestroyCommand.setDestroyVolumes(true);

    val exitCode = clusterDestroyCommand.call();
    assertEquals(0, exitCode);

    log.info(testTextTerminal.getOutputAndReset(false));
    log.info("sdf");
  }

  @Test
  public void testEgo() {
    val client = egoClientFactory.buildNoAuthEgoClient("https://ego.icgc-argo.org/api");
    val out = client.getPublicKey();
    log.info("sdfsdf");
  }
}
