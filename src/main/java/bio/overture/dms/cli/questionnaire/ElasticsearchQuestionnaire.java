package bio.overture.dms.cli.questionnaire;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.ElasticsearchConfig;
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

  public ElasticsearchConfig buildConfig() {
    String password = null;
    val apiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class,
                "What port would you like to expose the elasticsearch http api on?",
                true,
                ElasticsearchConfig.DEFAULT_PORT)
            .getAnswer();

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
        .hostPort(apiPort)
        .security(
            ElasticsearchConfig.Security.builder()
                .enabled(enableSecurity)
                .rootPassword(password)
                .build())
        .build();
  }
}
