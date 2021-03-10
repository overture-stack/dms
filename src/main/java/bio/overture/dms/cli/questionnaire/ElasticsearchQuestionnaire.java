package bio.overture.dms.cli.questionnaire;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.ElasticsearchConfig;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
import bio.overture.dms.core.model.dmsconfig.MaestroConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.ELASTICSEARCH;
import static bio.overture.dms.compose.model.ComposeServiceResources.MAESTRO;

@Component
public class ElasticsearchQuestionnaire {

  private final QuestionFactory questionFactory;

  @Autowired
  public ElasticsearchQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  public ElasticsearchConfig buildConfig(ClusterRunModes runModes, GatewayConfig gatewayConfig) {
    String password = null;
    val info = resolveServiceConnectionInfo(runModes, gatewayConfig, questionFactory, ELASTICSEARCH.toString(), ElasticsearchConfig.DEFAULT_PORT);
    val enableSecurity =
        questionFactory
            .newDefaultSingleQuestion(
                Boolean.class,
                "Do you want to enable elasticsearch user authentication?",
                true,
                true)
            .getAnswer();

    if (enableSecurity) {
      password =
          questionFactory
              .newPasswordQuestion("What should the superuser (elastic) password be?")
              .getAnswer();
    }

    return ElasticsearchConfig.builder()
        .hostPort(info.port)
        .url(info.serverUrl)
        .security(
            ElasticsearchConfig.Security.builder()
                .enabled(enableSecurity)
                .rootPassword(password)
                .build())
        .build();
  }
}
