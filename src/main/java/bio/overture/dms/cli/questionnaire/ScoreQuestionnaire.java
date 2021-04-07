package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.model.Constants.ScoreQuestions.*;
import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.getDefaultValue;
import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.*;
import static bio.overture.dms.core.util.RandomGenerator.createRandomGenerator;
import static java.util.Objects.isNull;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.AppCredential;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig.ScoreApiConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig.ScoreS3Config;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.core.util.RandomGenerator;
import java.net.URL;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScoreQuestionnaire {

  /** Constants */
  private static final int DEFAULT_EGO_CLIENT_SECRET_LENGTH = 30;

  private static final int DEFAULT_S3_ACCESSKEY_LENGTH = 20;
  private static final int DEFAULT_S3_SECRETKEY_LENGTH = 40;
  private static final RandomGenerator RANDOM_GENERATOR =
      createRandomGenerator(ScoreQuestionnaire.class.getSimpleName());
  private static final String DEFAULT_SCORE_APP_CLIENT_ID = SCORE_API.toString();


  /** Dependencies */
  private final QuestionFactory questionFactory;

  @Autowired
  public ScoreQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  public ScoreConfig buildScoreConfig(ClusterRunModes clusterRunMode, GatewayConfig gatewayConfig, ScoreConfig existingConfig) {
    val s3Config = processScoreS3Config(gatewayConfig, existingConfig);
    val apiConfig = processScoreApiConfig(gatewayConfig, existingConfig);
    return ScoreConfig.builder().api(apiConfig).s3(s3Config).build();
  }

  private static AppCredential processScoreAppCreds() {
    return AppCredential.builder()
        .name(DEFAULT_SCORE_APP_NAME)
        .clientId(DEFAULT_SCORE_APP_CLIENT_ID)
        .clientSecret(RANDOM_GENERATOR.generateRandomAsciiString(DEFAULT_EGO_CLIENT_SECRET_LENGTH))
        .build();
  }

  @SneakyThrows
  private ScoreApiConfig processScoreApiConfig(GatewayConfig gatewayConfig, ScoreConfig existingConfig) {
    val apiBuilder = ScoreApiConfig.builder();

    val info =
        resolveServiceConnectionInfo(
            gatewayConfig, SCORE_API.toString(), 9020);

    apiBuilder.hostPort(info.port);
    apiBuilder.url(info.serverUrl);

    val objectBucket =
        questionFactory
            .newDefaultSingleQuestion(String.class, OBJECT_BUCKET_NAME, true, getDefaultValue(() -> existingConfig.getApi().getObjectBucket() , "dms.object", isNull(existingConfig)))
            .getAnswer();
    apiBuilder.objectBucket(objectBucket);

    val stateBucket =
        questionFactory
            .newDefaultSingleQuestion(String.class, STATE_BUCKET_NAME, true, getDefaultValue(() -> existingConfig.getApi().getStateBucket() , "dms.state", isNull(existingConfig)))
            .getAnswer();

    apiBuilder.stateBucket(stateBucket);
    if (isNull(existingConfig) || isNull(existingConfig.getApi().getAppCredential())) {
      apiBuilder.appCredential(processScoreAppCreds());
    } else {
      apiBuilder.appCredential(existingConfig.getApi().getAppCredential());
    }

    return apiBuilder.build();
  }

  @SneakyThrows
  private ScoreS3Config processScoreS3Config(GatewayConfig gatewayConfig, ScoreConfig existingConfig) {
    if (!isNull(existingConfig) && !isNull(existingConfig.getS3())) {
      val reuseConfig =
          questionFactory
              .newDefaultSingleQuestion(Boolean.class, YOU_HAVE_ALREADY_CONFIGURED_AN_S3, false, null)
              .getAnswer();
      if (reuseConfig) {
        return existingConfig.getS3();
      }
    }

    val s3Builder = ScoreS3Config.builder();
    val useExternalS3 =
        questionFactory
            .newDefaultSingleQuestion(Boolean.class, EXISTING_S3_YN, false, null)
            .getAnswer();

    val useMinio = !useExternalS3;
    s3Builder.useMinio(useMinio);

    if (useExternalS3) {
      val useAwsS3 =
          questionFactory
              .newDefaultSingleQuestion(Boolean.class, USING_AWS_S3, false, null)
              .getAnswer();

      if (useAwsS3) {
        val awsS3Region =
            questionFactory
                .newDefaultSingleQuestion(String.class, AWS_S3_REGION, false, null)
                .getAnswer();

        // https://docs.aws.amazon.com/general/latest/gr/rande.html#regional-endpoints
        val awsS3Url = new URL("https://s3." + awsS3Region + ".amazonaws.com");
        s3Builder.s3Region(awsS3Region);
        s3Builder.url(awsS3Url);
      } else {
        URL externalS3Url =
            questionFactory.newUrlSingleQuestion(EXT_S3_URL, false, null).getAnswer();

        // remove the trailing slash from the url because it breaks score
        if (externalS3Url.toString().endsWith("/")) {
          externalS3Url =
              new URL(externalS3Url.toString().substring(0, externalS3Url.toString().length() - 1));
        }
        s3Builder.url(externalS3Url);
      }

      val s3AccessKey =
          questionFactory
              .newDefaultSingleQuestion(String.class, S3_ACCESS_KEY, false, null)
              .getAnswer();
      s3Builder.accessKey(s3AccessKey);

      val s3SecretKey =
          questionFactory
              .newDefaultSingleQuestion(String.class, S3_SECRET_KEY, false, null)
              .getAnswer();
      s3Builder.secretKey(s3SecretKey);
    } else {
      // Use minio
      val generateMinioCreds =
          questionFactory
              .newDefaultSingleQuestion(Boolean.class, MINIO_CREATE_CREDS, false, null)
              .getAnswer();

      String minioAccessKey;
      String minioSecretKey;
      if (generateMinioCreds) {
        minioAccessKey = RANDOM_GENERATOR.generateRandomAsciiString(DEFAULT_S3_ACCESSKEY_LENGTH);
        minioSecretKey = RANDOM_GENERATOR.generateRandomAsciiString(DEFAULT_S3_SECRETKEY_LENGTH);
      } else {
        minioAccessKey =
            questionFactory
                .newDefaultSingleQuestion(String.class, MINIO_ACCESS_KEY, false, null)
                .getAnswer();
        minioSecretKey =
            questionFactory
                .newDefaultSingleQuestion(String.class, MINIO_SECRET_KEY, false, null)
                .getAnswer();
      }
      s3Builder.accessKey(minioAccessKey);
      s3Builder.secretKey(minioSecretKey);

      val info =
          resolveServiceConnectionInfo(
              gatewayConfig, MINIO_API.toString(), 9021);
      s3Builder.hostPort(info.port);
      s3Builder.url(info.serverUrl);
    }
    return s3Builder.build();
  }
}
