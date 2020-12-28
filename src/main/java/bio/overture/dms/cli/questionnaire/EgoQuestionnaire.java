package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.ClusterRunModes.LOCAL;
import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.ClusterRunModes.PRODUCTION;
import static bio.overture.dms.core.util.RandomGenerator.createRandomGenerator;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.HOURS;

import bio.overture.dms.cli.command.config.ConfigBuildCommand;
import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.questionnaire.DmsQuestionnaire.ClusterRunModes;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.AppCredentials;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.JwtConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.JwtConfig.JwtDuration;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.SSOClientConfig;
import bio.overture.dms.core.util.RandomGenerator;
import java.net.URL;
import java.util.function.BiConsumer;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EgoQuestionnaire {

  /** Constants */
  private static final String DEFAULT_DMS_APP_NAME = "dms";

  private static final String DEFAULT_UI_APP_NAME = "ego-ui";

  private static final String DEFAULT_DMS_APP_CLIENT_ID = "dms";
  private static final String DEFAULT_UI_APP_CLIENT_ID = "ego-ui";
  private static final int DEFAULT_PASSWORD_LENGTH = 30;
  private static final RandomGenerator RANDOM_GENERATOR =
      createRandomGenerator(ConfigBuildCommand.class.getSimpleName());

  /** Dependencies */
  private final QuestionFactory questionFactory;

  @Autowired
  public EgoQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  // TODO: add inputEgoConfig, so that an existing ego config can be updated
  public EgoConfig buildEgoConfig(ClusterRunModes clusterRunMode) {
    val b = EgoConfig.builder();

    val apiKeyDurationDays =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "How many days should api keys be valid for?", true, 30)
            .getAnswer();
    b.apiTokenDurationDays(apiKeyDurationDays);

    val jwtUserDurationHours =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "How many hours should USER JWTs be valid for?", true, 3)
            .getAnswer();

    val jwtAppDurationHours =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "How many hours should APP JWTs be valid for?", true, 3)
            .getAnswer();

    b.jwt(
        JwtConfig.builder()
            .app(new JwtDuration(HOURS.toMillis(jwtUserDurationHours)))
            .app(new JwtDuration(HOURS.toMillis(jwtAppDurationHours)))
            .build());

    val refreshTokenDurationHours =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "How many hours should refresh tokens be valid for?", true, 12)
            .getAnswer();
    b.refreshTokenDurationMS(HOURS.toMillis(refreshTokenDurationHours));

    val apiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the EGO api on?", true, 9000)
            .getAnswer();
    b.apiHostPort(apiPort);

    URL serverUrl;
    if (clusterRunMode == PRODUCTION) {
      serverUrl =
          questionFactory
              .newUrlSingleQuestion("What will the EGO server base url be?", false, null)
              .getAnswer();
    } else if (clusterRunMode == LOCAL) {
      serverUrl = createLocalhostUrl(apiPort);
    } else {
      throw new IllegalStateException(
          format(
              "The clusterRunMode '%s' is unknown and cannot be processed", clusterRunMode.name()));
    }
    b.serverUrl(serverUrl);

    val isSetDBPassword =
        questionFactory
            .newDefaultSingleQuestion(
                Boolean.class, "Would you like to set the database password for EGO?", false, null)
            .getAnswer();

    if (isSetDBPassword) {
      val dbPassword =
          questionFactory.newPasswordQuestion("What should the EGO db password be?").getAnswer();
      b.databasePassword(dbPassword);
    }

    val dbPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the EGO db on?", true, 9001)
            .getAnswer();
    b.dbHostPort(dbPort);

    val egoConfig = b.build();
    egoConfig.setSso(new EgoConfig.SSOConfig());

    val ssoProviderSelection =
        questionFactory
            .newMCQuestion(
                SSOProviders.class, "What SSO providers would you like to enable?", false, null)
            .getAnswer();

    ssoProviderSelection.forEach(
        p -> {
          val clientConfig = processSSOClientConfig(clusterRunMode, serverUrl, p.toString());
          p.setClientConfig(egoConfig.getSso(), clientConfig);
        });

    egoConfig.setDmsAppCredentials(processDmsAppCreds(egoConfig));
    egoConfig.setUiAppCredentials(processUiAppCreds());
    return egoConfig;
  }

  private SSOClientConfig processSSOClientConfig(
      ClusterRunModes clusterRunMode, URL serverUrl, String providerType) {
    val clientConfigBuilder = SSOClientConfig.builder();

    val clientId =
        questionFactory
            .newDefaultSingleQuestion(
                String.class, format("What is the %s client id?", providerType), false, null)
            .getAnswer();
    clientConfigBuilder.clientId(clientId);

    val clientSecret =
        questionFactory
            .newDefaultSingleQuestion(
                String.class, format("What is the %s client secret?", providerType), false, null)
            .getAnswer();
    clientConfigBuilder.clientSecret(clientSecret);

    var preEstablishedRedirectUri =
        format("%s/oauth/login/%s", serverUrl, providerType.toLowerCase());
    if (clusterRunMode == PRODUCTION) {
      preEstablishedRedirectUri =
          questionFactory
              .newDefaultSingleQuestion(
                  String.class,
                  format("What is the %s pre-established redirect url?", providerType),
                  true,
                  preEstablishedRedirectUri)
              .getAnswer();
    }
    clientConfigBuilder.preEstablishedRedirectUri(preEstablishedRedirectUri);
    return clientConfigBuilder.build();
  }

  private AppCredentials processUiAppCreds() {
    return AppCredentials.builder()
        .name(DEFAULT_DMS_APP_NAME)
        .clientId(DEFAULT_UI_APP_CLIENT_ID)
        .clientSecret(RANDOM_GENERATOR.generateRandomAsciiString(DEFAULT_PASSWORD_LENGTH))
        .build();
  }

  private AppCredentials processDmsAppCreds(EgoConfig egoConfig) {
    if (isNull(egoConfig.getDmsAppCredentials())) {
      // doesnt exist, ask questions
      val clientId =
          questionFactory
              .newDefaultSingleQuestion(
                  String.class,
                  "The EGO application with name '"
                      + DEFAULT_DMS_APP_NAME
                      + "' was not yet configured. Please input the clientId?",
                  true,
                  DEFAULT_DMS_APP_CLIENT_ID)
              .getAnswer();

      val clientSecret = generateAppSecret();
      return AppCredentials.builder()
          .name(DEFAULT_DMS_APP_NAME)
          .clientId(clientId)
          .clientSecret(clientSecret)
          .build();
    } else {
      // Ask if want to updated creds
      val doUpdate =
          questionFactory
              .newDefaultSingleQuestion(
                  Boolean.class,
                  "A dms application configuration exists, would you like to update it?",
                  true,
                  false)
              .getAnswer();
      if (doUpdate) {
        val creds = egoConfig.getDmsAppCredentials();
        // Do the update
        val appName =
            questionFactory
                .newDefaultSingleQuestion(
                    String.class,
                    "The EGO application with name '"
                        + DEFAULT_DMS_APP_NAME
                        + "' is already configured. "
                        + "Please input the name to update: ",
                    true,
                    creds.getName())
                .getAnswer();

        val clientId =
            questionFactory
                .newDefaultSingleQuestion(
                    String.class,
                    "The EGO application '"
                        + DEFAULT_DMS_APP_CLIENT_ID
                        + "' is already configured. Please input the clientId to update?",
                    true,
                    creds.getClientId())
                .getAnswer();

        val clientSecret = generateAppSecret();

        return AppCredentials.builder()
            .name(appName)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
      }
      return egoConfig.getDmsAppCredentials();
    }
  }

  private String generateAppSecret() {
    val charCount =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class,
                "How many characters should the randomly generated clientSecret contain?",
                true,
                DEFAULT_PASSWORD_LENGTH)
            .getAnswer();

    return RANDOM_GENERATOR.generateRandomAsciiString(charCount);
  }

  @SneakyThrows
  private static URL createLocalhostUrl(int port) {
    return new URL("http://localhost:" + port);
  }

  public enum SSOProviders {
    GOOGLE(EgoConfig.SSOConfig::setGoogle),
    LINKEDIN(EgoConfig.SSOConfig::setLinkedin),
    GITHUB(EgoConfig.SSOConfig::setGithub),
    FACEBOOK(EgoConfig.SSOConfig::setFacebook),
    ORCID(EgoConfig.SSOConfig::setOrcid);

    private final BiConsumer<EgoConfig.SSOConfig, SSOClientConfig> setter;

    SSOProviders(BiConsumer<EgoConfig.SSOConfig, SSOClientConfig> setter) {
      this.setter = setter;
    }

    public void setClientConfig(EgoConfig.SSOConfig s, SSOClientConfig clientConfig) {
      setter.accept(s, clientConfig);
    }
  }
}
