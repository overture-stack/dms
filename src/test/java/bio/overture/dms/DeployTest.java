package bio.overture.dms;

import bio.overture.dms.cli.command.cluster.ClusterApplyCommand;
import bio.overture.dms.cli.command.cluster.ClusterDestroyCommand;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Should only be used for local testing")
public class DeployTest {

  @Autowired private ClusterApplyCommand clusterApplyCommand;
  @Autowired private ClusterDestroyCommand clusterDestroyCommand;

  @Test
  @SneakyThrows
  public void testDeploy() {
    clusterApplyCommand.call();
    log.info("sdf");
  }

  @Test
  @SneakyThrows
  public void testDestroy() {
    clusterDestroyCommand.setForce(true);
    clusterDestroyCommand.setDestroyVolumes(false);
    clusterDestroyCommand.call();
    log.info("sdf");
  }
}
