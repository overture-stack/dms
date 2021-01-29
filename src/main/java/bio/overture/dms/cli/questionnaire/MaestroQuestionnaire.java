package bio.overture.dms.cli.questionnaire;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.MaestroConfig;
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

  public MaestroConfig buildConfig() {
    val apiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the Maestro on?",
                true, MaestroConfig.DEFAULT_PORT)
            .getAnswer();

    return MaestroConfig.builder()
        .hostPort(apiPort)
        .build();
  }
}
