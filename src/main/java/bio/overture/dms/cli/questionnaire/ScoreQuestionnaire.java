package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.createLocalhostUrl;
import static bio.overture.dms.compose.model.ComposeServiceResources.SCORE_API;
import static bio.overture.dms.core.model.enums.ClusterRunModes.LOCAL;
import static bio.overture.dms.core.model.enums.ClusterRunModes.PRODUCTION;
import static bio.overture.dms.core.util.RandomGenerator.createRandomGenerator;
import static java.lang.String.format;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.AppCredential;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig.ScoreApiConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig.ScoreS3Config;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.core.util.Nullable;
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
  private static final String DEFAULT_SCORE_APP_NAME = SCORE_API.toString();

  /** Dependencies */
  private final QuestionFactory questionFactory;

  @Autowired
  public ScoreQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  public ScoreConfig buildScoreConfig(
      @Nullable URL dmsGatewayUrl, @NonNull ClusterRunModes clusterRunMode) {
    val s3Config = processScoreS3Config(dmsGatewayUrl, clusterRunMode);
    val apiConfig = processScoreApiConfig(clusterRunMode);
    return ScoreConfig.builder().api(apiConfig).s3(s3Config).build();
  }

  private static AppCredential processScoreAppCreds() {
    return AppCredential.builder()
        .name(DEFAULT_SCORE_APP_NAME)
        .clientId(DEFAULT_SCORE_APP_CLIENT_ID)
        .clientSecret(RANDOM_GENERATOR.generateRandomAsciiString(DEFAULT_EGO_CLIENT_SECRET_LENGTH))
        .build();
  }

  private ScoreApiConfig processScoreApiConfig(ClusterRunModes clusterRunMode) {
    val apiBuilder = ScoreApiConfig.builder();

    val objectBucket =
        questionFactory
            .newDefaultSingleQuestion(
                String.class,
                "What is the name of the OBJECT bucket used for SCORE?",
                true,
                "dms.object")
            .getAnswer();
    apiBuilder.objectBucket(objectBucket);

    val stateBucket =
        questionFactory
            .newDefaultSingleQuestion(
                String.class,
                "What is the name of the STATE bucket used for SCORE?",
                true,
                "dms.state")
            .getAnswer();
    apiBuilder.stateBucket(stateBucket);

    URL serverUrl;
    if (clusterRunMode == PRODUCTION) {
      serverUrl =
          questionFactory
              .newUrlSingleQuestion("What will the SCORE server base url be?", false, null)
              .getAnswer();
    } else if (clusterRunMode == LOCAL) {
      val apiPort =
          questionFactory
              .newDefaultSingleQuestion(
                  Integer.class, "What port would you like to expose the SCORE api on?", true, 9020)
              .getAnswer();
      apiBuilder.hostPort(apiPort);

      serverUrl = createLocalhostUrl(apiPort);
    } else {
      throw new IllegalStateException(
          format(
              "The clusterRunMode '%s' is unknown and cannot be processed", clusterRunMode.name()));
    }
    apiBuilder.url(serverUrl);
    apiBuilder.appCredential(processScoreAppCreds());
    return apiBuilder.build();
  }

  @SneakyThrows
  private ScoreS3Config processScoreS3Config(URL dmsGatewayUrl, ClusterRunModes clusterRunMode) {
    val s3Builder = ScoreS3Config.builder();

    val useExternalS3 =
        questionFactory
            .newDefaultSingleQuestion(
                Boolean.class,
                "Do you have an existing S3 service you would like to use with the SCORE service?",
                false,
                null)
            .getAnswer();

    val useMinio = !useExternalS3;
    s3Builder.useMinio(useMinio);

    if (useExternalS3) {
      val useAwsS3 =
          questionFactory
              .newDefaultSingleQuestion(Boolean.class, "Will you be using AWS S3?", false, null)
              .getAnswer();

      if (useAwsS3) {
        val awsS3Region =
            questionFactory
                .newDefaultSingleQuestion(String.class, "What is the s3 region?", false, null)
                .getAnswer();

        // https://docs.aws.amazon.com/general/latest/gr/rande.html#regional-endpoints
        val awsS3Url = new URL("https://s3." + awsS3Region + ".amazonaws.com");
        s3Builder.s3Region(awsS3Region);
        s3Builder.url(awsS3Url);
      } else {
        val externalS3Url =
            questionFactory
                .newUrlSingleQuestion("What is the URL of the S3 service?", false, null)
                .getAnswer();
        s3Builder.url(externalS3Url);
      }

      val s3AccessKey =
          questionFactory
              .newDefaultSingleQuestion(String.class, "What is the S3 accessKey?", false, null)
              .getAnswer();
      s3Builder.accessKey(s3AccessKey);

      val s3SecretKey =
          questionFactory
              .newDefaultSingleQuestion(String.class, "What is the S3 accessKey?", false, null)
              .getAnswer();
      s3Builder.secretKey(s3SecretKey);
    } else {
      // Use minio
      val generateMinioCreds =
          questionFactory
              .newDefaultSingleQuestion(
                  Boolean.class,
                  "MinIO will be used as the SCORE S3 Backend. Would you like to credentials to be generated? "
                      + "If no, you will have to input them in the subsequent questions.",
                  false,
                  null)
              .getAnswer();

      String minioAccessKey;
      String minioSecretKey;
      if (generateMinioCreds) {
        minioAccessKey = RANDOM_GENERATOR.generateRandomAsciiString(DEFAULT_S3_ACCESSKEY_LENGTH);
        minioSecretKey = RANDOM_GENERATOR.generateRandomAsciiString(DEFAULT_S3_SECRETKEY_LENGTH);
      } else {
        minioAccessKey =
            questionFactory
                .newDefaultSingleQuestion(
                    String.class, "What should the MinIO accessKey be?", false, null)
                .getAnswer();
        minioSecretKey =
            questionFactory
                .newDefaultSingleQuestion(
                    String.class, "What should the MinIO secretKey be?", false, null)
                .getAnswer();
      }
      s3Builder.accessKey(minioAccessKey);
      s3Builder.secretKey(minioSecretKey);

      // Only portforward when in local mode
      if (clusterRunMode == LOCAL) {
        val s3Port =
            questionFactory
                .newDefaultSingleQuestion(
                    Integer.class,
                    "What port would you like to expose the MINIO service on?",
                    true,
                    9021)
                .getAnswer();
        s3Builder.hostPort(s3Port);
        s3Builder.url(createLocalhostUrl(s3Port));
      } else if (clusterRunMode == PRODUCTION) {
        s3Builder.hostPort(null);
        s3Builder.url(resolveMinioProdUrl(dmsGatewayUrl));
      } else {
        throw new IllegalStateException(
            format(
                "The clusterRunMode '%s' is unknown and cannot be processed",
                clusterRunMode.name()));
      }
    }
    return s3Builder.build();
  }

  @SneakyThrows
  private static URL resolveMinioProdUrl(@NonNull URL dmsGatewayUrl) {
    return new URL(dmsGatewayUrl.toString() + "/minio");
  }

}
