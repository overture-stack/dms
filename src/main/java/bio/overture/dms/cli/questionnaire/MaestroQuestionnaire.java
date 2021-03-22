package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.MAESTRO;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
import bio.overture.dms.core.model.dmsconfig.MaestroConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MaestroQuestionnaire {

  private final QuestionFactory questionFactory;

  @Autowired
  public MaestroQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  public MaestroConfig buildConfig(ClusterRunModes runModes, GatewayConfig gatewayConfig) {

    val info =
        resolveServiceConnectionInfo(
            runModes,
            gatewayConfig,
            questionFactory,
            MAESTRO.toString(),
            MaestroConfig.DEFAULT_PORT);

    String aliasName =
        questionFactory
            .newDefaultSingleQuestion(
                String.class,
                "What should the file centric alias be (must be different from index name)?",
                true,
                MaestroConfig.FILE_CENTRIC_ALIAS_NAME)
            .getAnswer();

    String indexName =
        questionFactory
            .newDefaultSingleQuestion(
                String.class,
                "What should the file centric index name be (must be different from alias)?",
                true,
                MaestroConfig.FILE_CENTRIC_INDEX_NAME)
            .getAnswer();

    while (aliasName.trim().equalsIgnoreCase(indexName.trim())) {
      indexName =
          questionFactory
              .newDefaultSingleQuestion(
                  String.class,
                  "What should the file centric index be (must be different than alias name) ?",
                  true,
                  MaestroConfig.FILE_CENTRIC_INDEX_NAME)
              .getAnswer();
    }

    return MaestroConfig.builder()
        .hostPort(info.port)
        .url(info.serverUrl)
        .fileCentricIndexName(indexName)
        .fileCentricAlias(aliasName)
        .build();
  }
}
