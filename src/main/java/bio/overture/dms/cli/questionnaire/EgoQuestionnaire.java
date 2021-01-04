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
import bio.overture.dms.core.model.dmsconfig.EgoConfig2;
import bio.overture.dms.core.model.dmsconfig.EgoConfig2.JwtConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig2.JwtConfig.JwtDuration;
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
  public EgoConfig2 buildEgoConfig(ClusterRunModes clusterRunMode) {
    val apiConfig = processEgoApiConfig(clusterRunMode);
    val dbConfig = processEgoDbConfig();
    val uiConfig = processEgoUiConfig(apiConfig);
    return EgoConfig2.builder().api(apiConfig).db(dbConfig).ui(uiConfig).build();
  }

  @Deprecated
  public EgoConfig2 buildEgoConfig2(ClusterRunModes clusterRunMode) {
    val apiBuilder = EgoConfig2.EgoApiConfig.builder();
    val dbBuilder = EgoConfig2.EgoDbConfig.builder();
    val uiBuilder = EgoConfig2.EgoUiConfig.builder();

    val apiKeyDurationDays =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "How many days should api keys be valid for?", true, 30)
            .getAnswer();
    apiBuilder.tokenDurationDays(apiKeyDurationDays);

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

    apiBuilder.jwt(
        JwtConfig.builder()
            .app(new JwtDuration(HOURS.toMillis(jwtUserDurationHours)))
            .app(new JwtDuration(HOURS.toMillis(jwtAppDurationHours)))
            .build());

    val refreshTokenDurationHours =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "How many hours should refresh tokens be valid for?", true, 12)
            .getAnswer();
    apiBuilder.refreshTokenDurationMS(HOURS.toMillis(refreshTokenDurationHours));

    val apiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the EGO api on?", true, 9000)
            .getAnswer();
    apiBuilder.hostPort(apiPort);

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
    apiBuilder.url(serverUrl);

    val isSetDBPassword =
        questionFactory
            .newDefaultSingleQuestion(
                Boolean.class, "Would you like to set the database password for EGO?", false, null)
            .getAnswer();

    if (isSetDBPassword) {
      val dbPassword =
          questionFactory.newPasswordQuestion("What should the EGO db password be?").getAnswer();
      dbBuilder.databasePassword(dbPassword);
    }

    val dbPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the EGO db on?", true, 9001)
            .getAnswer();
    val dbConfig = dbBuilder.hostPort(dbPort).build();

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    val apiConfig = apiBuilder.build();
    apiConfig.setSso(new EgoConfig2.SSOConfig());

    val ssoProviderSelection =
        questionFactory
            .newMCQuestion(
                SSOProviders.class, "What SSO providers would you like to enable?", false, null)
            .getAnswer();

    ssoProviderSelection.forEach(
        p -> {
          val clientConfig = processSSOClientConfig(clusterRunMode, serverUrl, p.toString());
          p.setClientConfig(apiConfig.getSso(), clientConfig);
        });

    apiConfig.setDmsAppCredentials(processDmsAppCreds(apiConfig));

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    val uiConfig = uiBuilder.uiAppCredentials(processUiAppCreds(apiConfig)).build();

    return EgoConfig2.builder().api(apiConfig).db(dbConfig).ui(uiConfig).build();
  }

  private EgoConfig2.EgoUiConfig processEgoUiConfig(EgoConfig2.EgoApiConfig egoApiConfig) {
    return EgoConfig2.EgoUiConfig.builder()
        .uiAppCredentials(processUiAppCreds(egoApiConfig))
        //        .url() //TODO: egoUi.url must be populated
        .build();
  }

  private EgoConfig2.EgoApiConfig processEgoApiConfig(ClusterRunModes clusterRunMode) {
    val apiBuilder = EgoConfig2.EgoApiConfig.builder();
    val apiKeyDurationDays =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "How many days should api keys be valid for?", true, 30)
            .getAnswer();
    apiBuilder.tokenDurationDays(apiKeyDurationDays);

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

    apiBuilder.jwt(
        JwtConfig.builder()
            .app(new JwtDuration(HOURS.toMillis(jwtUserDurationHours)))
            .app(new JwtDuration(HOURS.toMillis(jwtAppDurationHours)))
            .build());

    val refreshTokenDurationHours =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "How many hours should refresh tokens be valid for?", true, 12)
            .getAnswer();
    apiBuilder.refreshTokenDurationMS(HOURS.toMillis(refreshTokenDurationHours));

    val apiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the EGO api on?", true, 9000)
            .getAnswer();
    apiBuilder.hostPort(apiPort);

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
    apiBuilder.url(serverUrl);

    val apiConfig = apiBuilder.build();

    apiConfig.setSso(new EgoConfig2.SSOConfig());

    val ssoProviderSelection =
        questionFactory
            .newMCQuestion(
                SSOProviders.class, "What SSO providers would you like to enable?", false, null)
            .getAnswer();

    ssoProviderSelection.forEach(
        p -> {
          val clientConfig = processSSOClientConfig(clusterRunMode, serverUrl, p.toString());
          p.setClientConfig(apiConfig.getSso(), clientConfig);
        });

    apiConfig.setDmsAppCredentials(processDmsAppCreds(apiConfig));
    return apiConfig;
  }

  private EgoConfig2.EgoDbConfig processEgoDbConfig() {
    val dbBuilder = EgoConfig2.EgoDbConfig.builder();

    val isSetDBPassword =
        questionFactory
            .newDefaultSingleQuestion(
                Boolean.class, "Would you like to set the database password for EGO?", false, null)
            .getAnswer();

    if (isSetDBPassword) {
      val dbPassword =
          questionFactory.newPasswordQuestion("What should the EGO db password be?").getAnswer();
      dbBuilder.databasePassword(dbPassword);
    }

    val dbPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the EGO db on?", true, 9001)
            .getAnswer();
    dbBuilder.hostPort(dbPort);

    return dbBuilder.build();
  }

  private EgoConfig2.SSOClientConfig processSSOClientConfig(
      ClusterRunModes clusterRunMode, URL serverUrl, String providerType) {
    val clientConfigBuilder = EgoConfig2.SSOClientConfig.builder();

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

  private EgoConfig2.AppCredentials processUiAppCreds(@NonNull EgoConfig2.EgoApiConfig apiConfig) {
    return EgoConfig2.AppCredentials.builder()
        .name(DEFAULT_DMS_APP_NAME)
        .clientId(DEFAULT_UI_APP_CLIENT_ID)
        .clientSecret(RANDOM_GENERATOR.generateRandomAsciiString(DEFAULT_PASSWORD_LENGTH))
        .redirectUri(
            "http://localhost:8080") // TODO: ego-ui url is baked in!!!! needs to be dynamic
        .build();
  }

  private EgoConfig2.AppCredentials processDmsAppCreds(EgoConfig2.EgoApiConfig apiConfig) {
    if (isNull(apiConfig.getDmsAppCredentials())) {
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
      return EgoConfig2.AppCredentials.builder()
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
        val creds = apiConfig.getDmsAppCredentials();
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

        return EgoConfig2.AppCredentials.builder()
            .name(appName)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
      }
      return apiConfig.getDmsAppCredentials();
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
    GOOGLE(EgoConfig2.SSOConfig::setGoogle),
    LINKEDIN(EgoConfig2.SSOConfig::setLinkedin),
    GITHUB(EgoConfig2.SSOConfig::setGithub),
    FACEBOOK(EgoConfig2.SSOConfig::setFacebook),
    ORCID(EgoConfig2.SSOConfig::setOrcid);

    private final BiConsumer<EgoConfig2.SSOConfig, EgoConfig2.SSOClientConfig> setter;

    SSOProviders(BiConsumer<EgoConfig2.SSOConfig, EgoConfig2.SSOClientConfig> setter) {
      this.setter = setter;
    }

    public void setClientConfig(EgoConfig2.SSOConfig s, EgoConfig2.SSOClientConfig clientConfig) {
      setter.accept(s, clientConfig);
    }
  }
}
