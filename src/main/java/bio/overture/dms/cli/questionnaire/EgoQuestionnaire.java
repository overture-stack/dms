package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.createLocalhostUrl;
import static bio.overture.dms.core.util.RandomGenerator.createRandomGenerator;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.HOURS;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.AppCredential;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.EgoApiConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.EgoDbConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.EgoUiConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.JwtConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.JwtConfig.JwtDuration;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.SSOClientConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.SSOConfig;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
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
      createRandomGenerator(EgoQuestionnaire.class.getSimpleName());

  /** Dependencies */
  private final QuestionFactory questionFactory;

  @Autowired
  public EgoQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  // TODO: add inputEgoConfig, so that an existing ego config can be updated
  public EgoConfig buildEgoConfig(GatewayConfig gatewayConfig) {
    val apiConfig = processEgoApiConfig(gatewayConfig);
    val dbConfig = processEgoDbConfig();
    val uiConfig = processEgoUiConfig(gatewayConfig);
    return EgoConfig.builder().api(apiConfig).db(dbConfig).ui(uiConfig).build();
  }

  @SneakyThrows
  private EgoUiConfig processEgoUiConfig(GatewayConfig dmsConfig) {
    val egoUiConfig = new EgoUiConfig();
    val apiPort =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "What port would you like to expose the EGO ui on?", true, 9002)
            .getAnswer();
    egoUiConfig.setHostPort(apiPort);

    URL serverUrl = createLocalhostUrl(egoUiConfig.getHostPort());
    egoUiConfig.setUrl(serverUrl);
    egoUiConfig.setUiAppCredential(processUiAppCreds(serverUrl));
    return egoUiConfig;
  }

  @SneakyThrows
  private EgoApiConfig processEgoApiConfig(GatewayConfig gatewayConfig) {
    val apiBuilder = EgoApiConfig.builder();
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


    val serverUrl = gatewayConfig.getUrl().toURI().resolve("/ego-api").toURL();
    apiBuilder.url(serverUrl);

    val apiConfig = apiBuilder.build();

    apiConfig.setSso(new SSOConfig());

    val ssoProviderSelection =
        questionFactory
            .newMCQuestion(
                SSOProviders.class, "What SSO providers would you like to enable?", false, null)
            .getAnswer();

    ssoProviderSelection.forEach(
        p -> {
          val clientConfig = processSSOClientConfig(serverUrl, p.toString());
          p.setClientConfig(apiConfig.getSso(), clientConfig);
        });

    apiConfig.setDmsAppCredential(processDmsAppCreds(apiConfig));
    return apiConfig;
  }

  private EgoDbConfig processEgoDbConfig() {
    val dbBuilder = EgoDbConfig.builder();

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

    return dbBuilder.build();
  }

  private SSOClientConfig processSSOClientConfig(URL serverUrl, String providerType) {
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
    clientConfigBuilder.preEstablishedRedirectUri(preEstablishedRedirectUri);
    return clientConfigBuilder.build();
  }

  private AppCredential processUiAppCreds(@NonNull URL redirectUri) {
    return AppCredential.builder()
        .name(DEFAULT_UI_APP_NAME)
        .clientId(DEFAULT_UI_APP_CLIENT_ID)
        .clientSecret(RANDOM_GENERATOR.generateRandomAsciiString(DEFAULT_PASSWORD_LENGTH))
        .redirectUri(redirectUri.toString())
        .build();
  }

  private AppCredential processDmsAppCreds(EgoApiConfig apiConfig) {
    if (isNull(apiConfig.getDmsAppCredential())) {
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
      return AppCredential.builder()
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
        val creds = apiConfig.getDmsAppCredential();
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

        return AppCredential.builder()
            .name(appName)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
      }
      return apiConfig.getDmsAppCredential();
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

  public enum SSOProviders {
    GOOGLE(SSOConfig::setGoogle),
    LINKEDIN(SSOConfig::setLinkedin),
    GITHUB(SSOConfig::setGithub),
    FACEBOOK(SSOConfig::setFacebook),
    ORCID(SSOConfig::setOrcid);

    private final BiConsumer<SSOConfig, SSOClientConfig> setter;

    SSOProviders(BiConsumer<SSOConfig, SSOClientConfig> setter) {
      this.setter = setter;
    }

    public void setClientConfig(SSOConfig s, SSOClientConfig clientConfig) {
      setter.accept(s, clientConfig);
    }
  }
}
