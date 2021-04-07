package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.model.Constants.MaestroQuestions.ES_PASSWORD;
import static bio.overture.dms.cli.model.Constants.MaestroQuestions.PASSWORD_CONFIGURED_ELASTICSEARCH;
import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.ELASTICSEARCH;
import static java.util.Objects.isNull;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.core.model.dmsconfig.ElasticsearchConfig;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
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

  public ElasticsearchConfig buildConfig(GatewayConfig gatewayConfig, @NonNull ElasticsearchConfig existingConfig, Terminal t) {
    String password;
    val info =
        resolveServiceConnectionInfo(
            gatewayConfig,
            ELASTICSEARCH.toString(),
            ElasticsearchConfig.DEFAULT_PORT);

    if (isNull(existingConfig) || isNull(existingConfig.getSecurity().getRootPassword())) {
      password = questionFactory.newPasswordQuestion(ES_PASSWORD).getAnswer();
    } else {
      t.println(PASSWORD_CONFIGURED_ELASTICSEARCH);
      password = existingConfig.getSecurity().getRootPassword();
    }

    return ElasticsearchConfig.builder()
        .hostPort(info.port)
        .url(info.serverUrl)
        .security(ElasticsearchConfig.Security.builder().rootPassword(password).build())
        .build();
  }
}
