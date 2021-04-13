package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.model.Constants.SongQuestions.PASSWORD_CONFIGURED_SONG_DB;
import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.SONG_API;
import static bio.overture.dms.core.util.RandomGenerator.createRandomGenerator;

import bio.overture.dms.cli.model.Constants;
import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.core.model.dmsconfig.AppCredential;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
import bio.overture.dms.core.model.dmsconfig.SongConfig;
import bio.overture.dms.core.model.dmsconfig.SongConfig.SongApiConfig;
import bio.overture.dms.core.model.dmsconfig.SongConfig.SongDbConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.core.util.RandomGenerator;
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

  public SongConfig buildSongConfig(@NonNull GatewayConfig gatewayConfig, SongConfig existingConfig, Terminal t) {
    val apiConfig = processSongApiConfig(gatewayConfig, existingConfig);
    val dbConfig = processSongDbConfig(existingConfig, t);
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
  private SongApiConfig processSongApiConfig(GatewayConfig gatewayConfig, SongConfig existingConfig) {
    val apiBuilder = SongApiConfig.builder();
    val info =
        resolveServiceConnectionInfo(gatewayConfig, SONG_API.toString(), 9010);
    apiBuilder.url(info.serverUrl);
    apiBuilder.hostPort(info.port);

    if (existingConfig == null || existingConfig.getApi().getAppCredential() == null) {
      apiBuilder.appCredential(processSongAppCreds());
    } else {
      apiBuilder.appCredential(existingConfig.getApi().getAppCredential());
    }
    return apiBuilder.build();
  }

  private SongDbConfig processSongDbConfig(SongConfig existingConfig, Terminal t) {
    if (existingConfig != null) {
      t.println(PASSWORD_CONFIGURED_SONG_DB);
      return existingConfig.getDb();
    }
    val dbBuilder = SongDbConfig.builder();
    val dbPassword =
        questionFactory.newPasswordQuestion(Constants.SongQuestions.PASSWORD).getAnswer();
    dbBuilder.databasePassword(dbPassword);

    return dbBuilder.build();
  }
}
