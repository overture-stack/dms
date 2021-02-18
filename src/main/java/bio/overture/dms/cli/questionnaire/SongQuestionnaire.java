package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.createLocalhostUrl;
import static bio.overture.dms.compose.model.ComposeServiceResources.SONG_API;
import static bio.overture.dms.core.model.enums.ClusterRunModes.LOCAL;
import static bio.overture.dms.core.model.enums.ClusterRunModes.PRODUCTION;
import static bio.overture.dms.core.util.RandomGenerator.createRandomGenerator;
import static java.lang.String.format;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.AppCredential;
import bio.overture.dms.core.model.dmsconfig.SongConfig;
import bio.overture.dms.core.model.dmsconfig.SongConfig.SongApiConfig;
import bio.overture.dms.core.model.dmsconfig.SongConfig.SongDbConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.core.util.RandomGenerator;
import java.net.URL;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SongQuestionnaire {

  /** Constants */
  private static final String DEFAULT_SONG_APP_NAME = SONG_API.toString();

  private static final String DEFAULT_SONG_APP_CLIENT_ID = SONG_API.toString();
  private static final int DEFAULT_PASSWORD_LENGTH = 30;
  private static final RandomGenerator RANDOM_GENERATOR =
      createRandomGenerator(SongQuestionnaire.class.getSimpleName());

  /** Dependencies */
  private final QuestionFactory questionFactory;

  @Autowired
  public SongQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  public SongConfig buildSongConfig(@NonNull ClusterRunModes clusterRunMode) {
    val apiConfig = processSongApiConfig(clusterRunMode);
    val dbConfig = processSongDbConfig();
    return SongConfig.builder().api(apiConfig).db(dbConfig).build();
  }

  private static AppCredential processSongAppCreds() {
    return AppCredential.builder()
        .name(DEFAULT_SONG_APP_NAME)
        .clientId(DEFAULT_SONG_APP_CLIENT_ID)
        .clientSecret(RANDOM_GENERATOR.generateRandomAsciiString(DEFAULT_PASSWORD_LENGTH))
        .build();
  }

  private SongApiConfig processSongApiConfig(ClusterRunModes clusterRunMode) {
    val apiBuilder = SongApiConfig.builder();

    val apiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the SONG api on?", true, 9010)
            .getAnswer();
    apiBuilder.hostPort(apiPort);

    URL serverUrl;
    if (clusterRunMode == PRODUCTION) {
      serverUrl =
          questionFactory
              .newUrlSingleQuestion("What will the SONG server base url be?", false, null)
              .getAnswer();
    } else if (clusterRunMode == LOCAL) {
      serverUrl = createLocalhostUrl(apiPort);
    } else {
      throw new IllegalStateException(
          format(
              "The clusterRunMode '%s' is unknown and cannot be processed", clusterRunMode.name()));
    }
    apiBuilder.url(serverUrl);
    apiBuilder.appCredential(processSongAppCreds());
    return apiBuilder.build();
  }

  private SongDbConfig processSongDbConfig() {
    val dbBuilder = SongDbConfig.builder();

    val isSetDBPassword =
        questionFactory
            .newDefaultSingleQuestion(
                Boolean.class, "Would you like to set the database password for SONG?", false, null)
            .getAnswer();

    if (isSetDBPassword) {
      val dbPassword =
          questionFactory.newPasswordQuestion("What should the SONG db password be?").getAnswer();
      dbBuilder.databasePassword(dbPassword);
    }

    val dbPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the SONG db on?", true, 9011)
            .getAnswer();
    dbBuilder.hostPort(dbPort);
    return dbBuilder.build();
  }

}
