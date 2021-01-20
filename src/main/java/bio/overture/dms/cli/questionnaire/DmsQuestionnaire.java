package bio.overture.dms.cli.questionnaire;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.compose.properties.ComposeProperties;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
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
  private final ComposeProperties composeProperties;
  private final SongQuestionnaire songQuestionnaire;
  private final ScoreQuestionnaire scoreQuestionnaire;

  @Autowired
  public DmsQuestionnaire(
      @NonNull QuestionFactory questionFactory,
      @NonNull BuildProperties buildProperties,
      @NonNull EgoQuestionnaire egoQuestionnaire,
      @NonNull ComposeProperties composeProperties,
      @NonNull SongQuestionnaire songQuestionnaire,
      ScoreQuestionnaire scoreQuestionnaire) {
    this.questionFactory = questionFactory;
    this.buildProperties = buildProperties;
    this.egoQuestionnaire = egoQuestionnaire;
    this.composeProperties = composeProperties;
    this.songQuestionnaire = songQuestionnaire;
    this.scoreQuestionnaire = scoreQuestionnaire;
  }

  public DmsConfig buildDmsConfig() {
    val clusterRunMode =
        questionFactory
            .newOneHotQuestion(
                ClusterRunModes.class, "Select the cluster mode to configure: ", false, null)
            .getAnswer();
    val egoConfig = egoQuestionnaire.buildEgoConfig(clusterRunMode);
    val songConfig = songQuestionnaire.buildSongConfig(clusterRunMode);
    val scoreConfig = scoreQuestionnaire.buildScoreConfig(clusterRunMode);
    return DmsConfig.builder()
        .clusterRunMode(clusterRunMode)
        .version(buildProperties.getVersion())
        .network(composeProperties.getNetwork())
        .ego(egoConfig)
        .song(songConfig)
        .score(scoreConfig)
        .build();
  }
}
