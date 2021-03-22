package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.DMS_UI;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.DmsUIConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
import bio.overture.dms.core.model.dmsconfig.MaestroConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.swarm.properties.DockerProperties;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DmsUIQuestionnaire {

  private final QuestionFactory questionFactory;
  private DockerProperties dockerProperties;

  @Autowired
  public DmsUIQuestionnaire(@NonNull QuestionFactory questionFactory, DockerProperties dockerProperties) {
    this.questionFactory = questionFactory;
    this.dockerProperties = dockerProperties;
  }

  public DmsUIConfig buildConfig(
      MaestroConfig maestroConfig, ClusterRunModes runModes, GatewayConfig gatewayConfig, EgoConfig egoConfig) {

    String email =
        questionFactory
            .newEmailQuestion(
                "What will the contact email be (will appear on the UI) ?",
                false,
                null
            ).getAnswer();

    String labName =
        questionFactory
            .newDefaultSingleQuestion(
                String.class,
                "Would you like to customise the portal title ?",
                true,
                "Data Management System")
            .getAnswer();

    String logoFileName = null;
    String assetsDirPath = null;

    try {
      val userDir = Paths.get(System.getProperty("user.home"));
      val dmsDir = userDir.resolve(".dms");
      // we need to figure out the assets directory host path (if running in docker)
      if (this.dockerProperties.getRunAs()) {
        assetsDirPath = Paths.get(dockerProperties.getDmsHomeHostPath()).resolve("assets").toAbsolutePath().toString();
      } else {
        assetsDirPath = dmsDir.resolve("assets").toAbsolutePath().toString();
      }

      // now we need to check the file we
      val dmsAssetsPath = dmsDir.resolve("assets").toAbsolutePath().toString();
      val exts = List.of("png", "svg", "jpg");
      val result = exts.stream().filter((ext) ->
        new File(Paths.get(dmsAssetsPath, "dms_logo.png").toString()).exists()
      ).findFirst();
      if (result.isPresent()) {
        logoFileName = "dms_logo." + result.get();
      }
    } catch (Exception e) {
      log.error("failed to resolve the assets directory");
    }

    val info =
        resolveServiceConnectionInfo(
            runModes, gatewayConfig, questionFactory, DMS_UI.toString(), DmsUIConfig.DEFAULT_PORT);
    String projectId =
        questionFactory
            .newDefaultSingleQuestion(
                String.class,
                "What will the project id (as created / will be created in Arranger) be ?",
                true,
                DmsUIConfig.ArrangerProjectConfig.DEFAULT_PROJECT_ID)
            .getAnswer();

    String projectName =
        questionFactory
            .newDefaultSingleQuestion(
                String.class,
                "What will the project name (as created / will be created in Arranger) be ?",
                true,
                DmsUIConfig.ArrangerProjectConfig.DEFAULT_PROJECT_NAME)
            .getAnswer();

    String elasticSearchIndexOrAlias =
        questionFactory
            .newDefaultSingleQuestion(
                String.class,
                "What will the Elasticsearch alias name (should match Maestro's alias and match Arranger's project configuration) be ?",
                true,
                maestroConfig.getFileCentricAlias())
            .getAnswer();

    return DmsUIConfig.builder()
        .projectConfig(
            DmsUIConfig.ArrangerProjectConfig.builder()
                .id(projectId)
                .name(projectName)
                .indexAlias(elasticSearchIndexOrAlias)
                .build())
        .url(info.serverUrl)
        .labName(labName)
        .logoFileName(logoFileName)
        .assetsDir(assetsDirPath)
        .ssoProviders(egoConfig.getApi()
            .getSso()
            .getConfiguredProviders()
            .stream()
            .map(p -> p.name().toUpperCase())
            .collect(Collectors.joining(","))
        )
        .adminEmail(email)
        .hostPort(info.port)
        .build();
  }
}
