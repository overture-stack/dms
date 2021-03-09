package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.createLocalhostUrl;
import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.SCORE_API;
import static bio.overture.dms.compose.model.ComposeServiceResources.SONG_API;
import static bio.overture.dms.core.util.RandomGenerator.createRandomGenerator;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.AppCredential;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
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

  public SongConfig buildSongConfig(ClusterRunModes clusterRunModes, @NonNull GatewayConfig gatewayConfig) {
    val apiConfig = processSongApiConfig(clusterRunModes, gatewayConfig);
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

  @SneakyThrows
  private SongApiConfig processSongApiConfig(ClusterRunModes clusterRunModes, GatewayConfig gatewayConfig) {
    val apiBuilder = SongApiConfig.builder();
    val info = resolveServiceConnectionInfo(clusterRunModes,
        gatewayConfig,
        questionFactory,
        SONG_API.toString(), 9010);
    apiBuilder.url(info.serverUrl);
    apiBuilder.hostPort(info.port);

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

    return dbBuilder.build();
  }
}
