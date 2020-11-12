package bio.overture.dms.infra.docker;

import bio.overture.dms.infra.service.EgoDeploymentService;
import bio.overture.dms.infra.spec.EgoSpec;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DmsApplicationTests {

  @Autowired
  private EgoDeploymentService egoDeploymentService;

  @Test
  public void teste(){
    val egoSpec = EgoSpec.builder()
        .host("https://example.org")
        .build();
    egoDeploymentService.deployEgo(egoSpec);
  }

  @Test
  void contextLoads() {}
}
