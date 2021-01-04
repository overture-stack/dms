package bio.overture.dms.compose.tasks.impl;

import static bio.overture.dms.core.util.RandomGenerator.createRandomGenerator;

import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.compose.tasks.PreDeployTask;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.util.RandomGenerator;
import bio.overture.dms.ego.EgoClientFactory;
import bio.overture.dms.ego.client.EgoClient.CreateApplicationRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/** This belongs in the cli package because it has context of the specific thing */
// TODO: issue --- this class will need an EgoClient, which
@Deprecated
@RequiredArgsConstructor
public class EgoUiDeployTask implements PreDeployTask {

  private static final RandomGenerator RANDOM_GENERATOR =
      createRandomGenerator(EgoUiDeployTask.class.getSimpleName());
  private final EgoClientFactory egoClientFactory;
  private final Terminal terminal;

  @Override
  public String getTaskName() {
    return "ego-ui";
  }

  @Override
  public void executePreDeployTask(@NonNull DmsConfig dmsConfig) {
    val egoClient = egoClientFactory.buildAuthDmsEgoClient(dmsConfig.getEgo());

    val result = egoClient.findApplicationByName(getTaskName());
    if (result.isPresent()) {
      val app = result.get();
      terminal.printStatusLn(
          "[%s]: EGO Application '%s' already exists, skipping creation.",
          getClass().getSimpleName(), app.getName());
    } else {
      val app =
          egoClient.createApplication(
              CreateApplicationRequest.builder()
                  .name(getTaskName())
                  .clientId(getTaskName())
                  .clientSecret(RANDOM_GENERATOR.generateRandomAsciiString(50))
                  .redirectUri("http://localhost:8080")
                  .build());
      terminal.printStatusLn(
          "[%s]: Created EGO Application '%s' successfully",
          getClass().getSimpleName(), app.getName());
    }
  }
}
