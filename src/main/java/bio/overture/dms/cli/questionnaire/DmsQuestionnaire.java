package bio.overture.dms.cli.questionnaire;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class DmsQuestionnaire {

  private final QuestionFactory questionFactory;
  private final BuildProperties buildProperties;
  private final EgoQuestionnaire egoQuestionnaire;

  @Autowired
  public DmsQuestionnaire(
      @NonNull QuestionFactory questionFactory,
      @NonNull BuildProperties buildProperties,
      @NonNull EgoQuestionnaire egoQuestionnaire) {
    this.questionFactory = questionFactory;
    this.buildProperties = buildProperties;
    this.egoQuestionnaire = egoQuestionnaire;
  }

  public DmsConfig buildDmsConfig() {
    val clusterRunMode =
        questionFactory
            .newOneHotQuestion(
                ClusterRunModes.class, "Select the cluster mode to configure: ", false, null)
            .getAnswer();
    val egoConfig = egoQuestionnaire.buildEgoConfig(clusterRunMode);
    return DmsConfig.builder()
        .clusterRunMode(clusterRunMode)
        .version(buildProperties.getVersion())
        .ego(egoConfig)
        .build();
  }

  public static enum ClusterRunModes {
    LOCAL,
    PRODUCTION;
  }
}
