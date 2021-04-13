package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.getDefaultValue;
import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.MAESTRO;
import static java.util.Objects.isNull;

import bio.overture.dms.cli.model.Constants;
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

  public MaestroConfig buildConfig(GatewayConfig gatewayConfig, MaestroConfig existingConfig) {

    val info =
        resolveServiceConnectionInfo(gatewayConfig,
            MAESTRO.toString(),
            MaestroConfig.DEFAULT_PORT);

    String aliasName =
        questionFactory
            .newDefaultSingleQuestion(
                String.class,
                Constants.MaestroQuestions.ALIAS,
                true,
                getDefaultValue(() -> existingConfig.getFileCentricAlias(),  MaestroConfig.FILE_CENTRIC_ALIAS_NAME, isNull(existingConfig)))
            .getAnswer();

    val indexNameQuestion = questionFactory
        .newDefaultSingleQuestion(
            String.class,
            Constants.MaestroQuestions.INDEX,
            true,
            getDefaultValue(() -> existingConfig.getFileCentricIndexName(), MaestroConfig.FILE_CENTRIC_INDEX_NAME, isNull(existingConfig)));

    String indexName = indexNameQuestion.getAnswer();
    while (aliasName.trim().equalsIgnoreCase(indexName.trim())) {
      indexName = indexNameQuestion.getAnswer();
    }

    return MaestroConfig.builder()
        .hostPort(info.port)
        .url(info.serverUrl)
        .fileCentricIndexName(indexName)
        .fileCentricAlias(aliasName)
        .build();
  }
}
