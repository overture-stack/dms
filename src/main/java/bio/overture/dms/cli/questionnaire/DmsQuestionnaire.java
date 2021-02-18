package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.core.model.enums.ClusterRunModes.PRODUCTION;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.compose.properties.ComposeProperties;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import java.net.URL;
import lombok.NonNull;
import lombok.SneakyThrows;
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
  private final ElasticsearchQuestionnaire elasticsearchQuestionnaire;
  private final MaestroQuestionnaire maestroQuestionnaire;
  private final ArrangerQuestionnaire arrangerQuestionnaire;
  private final DmsUIQuestionnaire dmsUIQuestionnaire;

  private final Terminal terminal;

  @Autowired
  public DmsQuestionnaire(
      @NonNull QuestionFactory questionFactory,
      @NonNull BuildProperties buildProperties,
      @NonNull EgoQuestionnaire egoQuestionnaire,
      @NonNull ComposeProperties composeProperties,
      @NonNull SongQuestionnaire songQuestionnaire,
      @NonNull ScoreQuestionnaire scoreQuestionnaire,
      @NonNull ElasticsearchQuestionnaire elasticsearchQuestionnaire,
      @NonNull MaestroQuestionnaire maestroQuestionnaire,
      @NonNull ArrangerQuestionnaire arrangerQuestionnaire,
      @NonNull DmsUIQuestionnaire dmsUIQuestionnaire,
      @NonNull Terminal terminal) {
    this.questionFactory = questionFactory;
    this.buildProperties = buildProperties;
    this.egoQuestionnaire = egoQuestionnaire;
    this.composeProperties = composeProperties;
    this.songQuestionnaire = songQuestionnaire;
    this.scoreQuestionnaire = scoreQuestionnaire;
    this.elasticsearchQuestionnaire = elasticsearchQuestionnaire;
    this.maestroQuestionnaire = maestroQuestionnaire;
    this.arrangerQuestionnaire = arrangerQuestionnaire;
    this.dmsUIQuestionnaire = dmsUIQuestionnaire;
    this.terminal = terminal;
  }

  public DmsConfig buildDmsConfig() {
    val clusterRunMode =
        questionFactory
            .newOneHotQuestion(
                ClusterRunModes.class, "Select the cluster mode to configure: ", false, null)
            .getAnswer();

    URL dmsGatewayUrl = null;
    if (clusterRunMode == PRODUCTION) {
      dmsGatewayUrl =
          questionFactory
              .newUrlSingleQuestion("What is the DMS Gateway URL?", false, null)
              .getAnswer();
    }

    printHeader("EGO");
    val egoConfig = egoQuestionnaire.buildEgoConfig(clusterRunMode);
    printHeader("SONG");
    val songConfig = songQuestionnaire.buildSongConfig(clusterRunMode);
    printHeader("SCORE");
    val scoreConfig = scoreQuestionnaire.buildScoreConfig(dmsGatewayUrl, clusterRunMode);
    printHeader("ELASTICSEARCH");
    val elasticConfig = elasticsearchQuestionnaire.buildConfig();
    printHeader("MAESTRO");
    val maestroConfig = maestroQuestionnaire.buildConfig();
    printHeader("ARRANGER");
    val arrangerConfig = arrangerQuestionnaire.buildConfig(clusterRunMode);
    printHeader("DMS UI");
    // we pass maestro's config to read the alias name to be used
    // in case the user changed the default.
    val dmsUIConfig = dmsUIQuestionnaire.buildConfig(maestroConfig);

    return DmsConfig.builder()
        .gatewayUrl(dmsGatewayUrl)
        .clusterRunMode(clusterRunMode)
        .version(buildProperties.getVersion())
        .network(composeProperties.getNetwork())
        .ego(egoConfig)
        .song(songConfig)
        .score(scoreConfig)
        .elasticsearch(elasticConfig)
        .maestro(maestroConfig)
        .dmsUI(dmsUIConfig)
        .arranger(arrangerConfig)
        .build();
  }

  @SneakyThrows
  static URL createLocalhostUrl(int port) {
    return new URL("http://localhost:" + port);
  }

  private void printHeader(@NonNull String title) {
    val line = "===============";
    terminal.println();
    terminal.println(line + "\n" + title + "\n" + line);
  }
}
