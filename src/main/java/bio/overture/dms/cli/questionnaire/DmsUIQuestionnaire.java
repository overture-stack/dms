package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.model.Constants.DmsUiQuestions.*;
import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.DMS_UI;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.core.model.dmsconfig.DmsUIConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
import bio.overture.dms.core.model.dmsconfig.MaestroConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.swarm.properties.DockerProperties;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DmsUIQuestionnaire {

  private final QuestionFactory questionFactory;
  private DockerProperties dockerProperties;
  private Terminal terminal;

  @Autowired
  public DmsUIQuestionnaire(
      @NonNull QuestionFactory questionFactory,
      DockerProperties dockerProperties,
      Terminal terminal) {
    this.questionFactory = questionFactory;
    this.dockerProperties = dockerProperties;
    this.terminal = terminal;
  }

  public DmsUIConfig buildConfig(
      MaestroConfig maestroConfig,
      ClusterRunModes runModes,
      GatewayConfig gatewayConfig,
      EgoConfig egoConfig) {

    String email = questionFactory.newEmailQuestion(EMAIL, false, null).getAnswer();

    String labName =
        questionFactory
            .newDefaultSingleQuestion(String.class, TITLE, true, "Data Management System")
            .getAnswer();

    String logoFileName = null;
    String assetsDirPath = null;

    try {
      val userDir = Paths.get(System.getProperty("user.home"));
      val dmsDir = userDir.resolve(".dms");
      // we need to figure out the assets directory host path (if running in docker)
      if (this.dockerProperties.getRunAs()) {
        assetsDirPath =
            Paths.get(dockerProperties.getDmsHomeHostPath())
                .resolve("assets")
                .toAbsolutePath()
                .toString();
      } else {
        assetsDirPath = dmsDir.resolve("assets").toAbsolutePath().toString();
      }

      // now we need to check the file we
      val dmsAssetsPath = dmsDir.resolve("assets").toAbsolutePath().toString();
      val exts = List.of("png", "svg", "jpg");
      val result =
          exts.stream()
              .filter(
                  (ext) ->
                      new File(Paths.get(dmsAssetsPath, "dms_logo." + ext).toString()).exists())
              .findFirst();
      if (result.isPresent()) {
        logoFileName = "dms_logo." + result.get();
      }
    } catch (Exception e) {
      log.error("failed to resolve the assets directory");
    }

    val info =
        resolveServiceConnectionInfo(
            runModes, gatewayConfig, questionFactory, DMS_UI.toString(), DmsUIConfig.DEFAULT_PORT);

    terminal.println(ARRANGER_QUESTIONS_NOTE);
    String projectId =
        questionFactory
            .newDefaultSingleQuestion(
                String.class, PROJ_ID, true, DmsUIConfig.ArrangerProjectConfig.DEFAULT_PROJECT_ID)
            .getAnswer();

    String projectName =
        questionFactory
            .newDefaultSingleQuestion(
                String.class,
                PROJ_NAME,
                true,
                DmsUIConfig.ArrangerProjectConfig.DEFAULT_PROJECT_NAME)
            .getAnswer();

    String elasticSearchIndexOrAlias =
        questionFactory
            .newDefaultSingleQuestion(
                String.class, ALIAS, true, maestroConfig.getFileCentricAlias())
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
        .ssoProviders(
            egoConfig.getApi().getSso().getConfiguredProviders().stream()
                .map(p -> p.name().toUpperCase())
                .collect(Collectors.joining(",")))
        .adminEmail(email)
        .hostPort(info.port)
        .build();
  }
}
