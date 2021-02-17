package bio.overture.dms.cli.questionnaire;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.ArrangerConfig;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArrangerQuestionnaire {

  private final QuestionFactory questionFactory;

  @Autowired
  public ArrangerQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  public ArrangerConfig buildConfig() {
    val apiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the Arranger API on?",
                true, ArrangerConfig.ArrangerApiConfig.DEFAULT_PORT)
            .getAnswer();

    val uiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the Arranger UI on?",
                true, ArrangerConfig.ArrangerUIConfig.DEFAULT_PORT)
            .getAnswer();

    return ArrangerConfig.builder()
        .api(ArrangerConfig.ArrangerApiConfig.builder().hostPort(apiPort).build())
        .ui(ArrangerConfig.ArrangerUIConfig.builder().hostPort(uiPort).build())
        .build();
  }
}
