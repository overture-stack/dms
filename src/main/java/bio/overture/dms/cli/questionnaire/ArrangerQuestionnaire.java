package bio.overture.dms.cli.questionnaire;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.ArrangerConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.createLocalhostUrl;
import static bio.overture.dms.core.model.enums.ClusterRunModes.LOCAL;
import static bio.overture.dms.core.model.enums.ClusterRunModes.PRODUCTION;
import static java.lang.String.format;

@Component
public class ArrangerQuestionnaire {

  private final QuestionFactory questionFactory;

  @Autowired
  public ArrangerQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  public ArrangerConfig buildConfig(ClusterRunModes clusterRunMode) {
    val apiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the Arranger API on?",
                true, ArrangerConfig.ArrangerApiConfig.DEFAULT_PORT)
            .getAnswer();

    URL serverUrl;
    if (clusterRunMode == PRODUCTION) {
      // TODO: check either this or pass the gateway url.
      serverUrl =
          questionFactory
              .newUrlSingleQuestion("What will the Arranger server base url be?", false, null)
              .getAnswer();
    } else if (clusterRunMode == LOCAL) {
      serverUrl = createLocalhostUrl(apiPort);
    } else {
      throw new IllegalStateException(
          format(
              "The clusterRunMode '%s' is unknown and cannot be processed", clusterRunMode.name()));
    }

    val uiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the Arranger UI on?",
                true, ArrangerConfig.ArrangerUIConfig.DEFAULT_PORT)
            .getAnswer();

    return ArrangerConfig.builder()
        .api(ArrangerConfig.ArrangerApiConfig.builder().hostPort(apiPort).url(serverUrl).build())
        .ui(ArrangerConfig.ArrangerUIConfig.builder().hostPort(uiPort).build())
        .build();
  }
}
