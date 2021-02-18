package bio.overture.dms.cli.questionnaire;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.DmsUIConfig;
import bio.overture.dms.core.model.dmsconfig.MaestroConfig;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class DmsUIQuestionnaire {

  private final QuestionFactory questionFactory;

  @Autowired
  public DmsUIQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  public DmsUIConfig buildConfig(MaestroConfig maestroConfig) {
    val uiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the DMS UI on?",
                true, DmsUIConfig.DEFAULT_PORT)
            .getAnswer();

    String projectId = questionFactory.newDefaultSingleQuestion(
        String.class, "What will the project id (as created / will be created in Arranger) be ?",
        true,
        DmsUIConfig.ArrangerProjectConfig.DEFAULT_PROJECT_ID
    ).getAnswer();

    String projectName = questionFactory.newDefaultSingleQuestion(
        String.class, "What will the project name (as created / will be created in Arranger) be ?",
        true,
        DmsUIConfig.ArrangerProjectConfig.DEFAULT_PROJECT_NAME
    ).getAnswer();

    String elasticSearchIndexOrAlias = questionFactory.newDefaultSingleQuestion(
        String.class, "What will the Elasticsearch alias name (should match Maestro's alias and match Arranger's project configuration) be ?",
        true,
        maestroConfig.getFileCentricAlias()
    ).getAnswer();

    return DmsUIConfig.builder()
        .projectConfig(
            DmsUIConfig.ArrangerProjectConfig.builder()
                .id(projectId)
                .name(projectName)
                .indexAlias(elasticSearchIndexOrAlias)
                .build()
        )
        .hostPort(uiPort)
        .build();
  }
}
