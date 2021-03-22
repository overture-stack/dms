package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.ELASTICSEARCH;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.ElasticsearchConfig;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchQuestionnaire {

  private final QuestionFactory questionFactory;

  @Autowired
  public ElasticsearchQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  public ElasticsearchConfig buildConfig(ClusterRunModes runModes, GatewayConfig gatewayConfig) {
    String password;
    val info =
        resolveServiceConnectionInfo(
            runModes,
            gatewayConfig,
            questionFactory,
            ELASTICSEARCH.toString(),
            ElasticsearchConfig.DEFAULT_PORT);

    password =
        questionFactory
            .newPasswordQuestion("What should the superuser (elastic) password be?")
            .getAnswer();

    return ElasticsearchConfig.builder()
        .hostPort(info.port)
        .url(info.serverUrl)
        .security(
            ElasticsearchConfig.Security.builder()
                .rootPassword(password)
                .build())
        .build();
  }
}
