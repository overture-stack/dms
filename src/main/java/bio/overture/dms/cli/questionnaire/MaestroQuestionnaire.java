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
                Integer.class,
                "What port would you like to expose the Maestro on?",
                true,
                MaestroConfig.DEFAULT_PORT)
            .getAnswer();

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
        .hostPort(apiPort)
        .fileCentricIndexName(indexName)
        .fileCentricAlias(aliasName)
        .build();
  }
}
