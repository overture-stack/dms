package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.model.Constants.EgoQuestions.*;
import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.getDefaultValue;
import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_API;
import static bio.overture.dms.compose.model.ComposeServiceResources.EGO_UI;
import static bio.overture.dms.core.util.RandomGenerator.createRandomGenerator;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.HOURS;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.terminal.Terminal;
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
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.core.util.RandomGenerator;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

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
  public EgoConfig buildEgoConfig(GatewayConfig gatewayConfig,
                                  EgoConfig existingConfig,
                                  Terminal t) {

    val apiConfig = processEgoApiConfig(gatewayConfig, existingConfig, existingConfig == null);
    val dbConfig = processEgoDbConfig(existingConfig, t);
    val uiConfig = processEgoUiConfig(gatewayConfig, existingConfig);
    return EgoConfig.builder().api(apiConfig).db(dbConfig).ui(uiConfig).build();
  }

  @SneakyThrows
  private EgoUiConfig processEgoUiConfig(GatewayConfig gatewayConfig, EgoConfig existingConfig) {
    val egoUiConfig = new EgoUiConfig();
    val info =
        resolveServiceConnectionInfo(gatewayConfig, EGO_UI.toString(), 9002);
    egoUiConfig.setHostPort(info.port);
    egoUiConfig.setUrl(info.serverUrl);
    if (isNull(existingConfig) || isNull(existingConfig.getUi().getUiAppCredential())) {
      egoUiConfig.setUiAppCredential(processUiAppCreds(info.serverUrl));
    } else {
      egoUiConfig.setUiAppCredential(existingConfig.getUi().getUiAppCredential());
    }
    return egoUiConfig;
  }

  @SneakyThrows
  private EgoApiConfig processEgoApiConfig(GatewayConfig gatewayConfig, EgoConfig existingConfig, boolean newConfig) {
    val apiBuilder = EgoApiConfig.builder();
    val apiKeyDurationDays =
        questionFactory.newDefaultSingleQuestion(Integer.class, API_KEY_DAYS,
            true, getDefaultValue(() -> existingConfig.getApi().getTokenDurationDays(), 30, newConfig)).getAnswer();
    apiBuilder.tokenDurationDays(apiKeyDurationDays);

    val jwtUserDurationHours =
        questionFactory
            .newDefaultSingleQuestion(Long.class, JWT_HOURS_DURATION, true,
                getDefaultValue(() -> TimeUnit.MILLISECONDS.toHours(existingConfig.getApi().getJwt().getUser().getDurationMs()), 3L, newConfig))
            .getAnswer();

    val jwtAppDurationHours =
        questionFactory
            .newDefaultSingleQuestion(Long.class, APP_JWT_DURATION_HOURS, true,
                getDefaultValue(() -> TimeUnit.MILLISECONDS.toHours(existingConfig.getApi().getJwt().getApp().getDurationMs()), 3L, newConfig))
            .getAnswer();

    apiBuilder.jwt(
        JwtConfig.builder()
            .app(new JwtDuration(HOURS.toMillis(jwtUserDurationHours)))
            .app(new JwtDuration(HOURS.toMillis(jwtAppDurationHours)))
            .build());

    val refreshTokenDurationHours =
        questionFactory
            .newDefaultSingleQuestion(
                Long.class, HOW_MANY_HOURS_SHOULD_REFRESH_TOKENS_BE_VALID_FOR,
        true,
                getDefaultValue(() -> TimeUnit.MILLISECONDS.toHours(existingConfig.getApi().getRefreshTokenDurationMS()), 12L, newConfig))
            .getAnswer();

    apiBuilder.refreshTokenDurationMS(HOURS.toMillis(refreshTokenDurationHours));

    val info = resolveServiceConnectionInfo(gatewayConfig, EGO_API.toString(), 9000);
    apiBuilder.hostPort(info.port);
    apiBuilder.url(info.serverUrl);

    val apiConfig = apiBuilder.build();

    configureSSOProviders(existingConfig, info, apiConfig);

    if (isNull(existingConfig) || isNull(existingConfig.getApi().getDmsAppCredential())) {
      apiConfig.setDmsAppCredential(processDmsAppCreds(apiConfig));
    } else {
      apiConfig.setDmsAppCredential(existingConfig.getApi().getDmsAppCredential());
    }

    return apiConfig;
  }

  private void configureSSOProviders(EgoConfig existingConfig, DmsQuestionnaire.ServiceUrlInfo info, EgoApiConfig apiConfig) {
    if (!isNull(existingConfig) && !isNull(existingConfig.getApi().getSso())) {
      val reuseConfigurations =
          questionFactory
              .newDefaultSingleQuestion(Boolean.class, "You have already configured the SSO providers, do you want to keep the same configurations ?", false, null)
              .getAnswer();

      if (reuseConfigurations) {
        apiConfig.setSso(existingConfig.getApi().getSso());
        return;
      }
    }

    apiConfig.setSso(new SSOConfig());
    boolean answered = false;
    List<SSOProviders> ssoProviderSelection = null;
    while (!answered) {
      ssoProviderSelection =
          questionFactory
              .newMCQuestion(SSOProviders.class, IDENTITY_PROVIDERS, false, null)
              .getAnswer();
      if (ssoProviderSelection != null && ssoProviderSelection.size() > 0) {
        answered = true;
      }
    }

    ssoProviderSelection.forEach(
        p -> {
          val clientConfig = processSSOClientConfig(info.serverUrl, p.toString(), existingConfig);
          p.setClientConfig(apiConfig.getSso(), clientConfig);
        });
  }



  private EgoDbConfig processEgoDbConfig(EgoConfig existingConfig, Terminal t) {
    if (existingConfig != null) {
      t.println("A password is already configured for EGO db.");
      return existingConfig.getDb();
    }
    val dbBuilder = EgoDbConfig.builder();
    val dbPassword = questionFactory.newPasswordQuestion(PASSWORD).getAnswer();
    dbBuilder.databasePassword(dbPassword);
    return dbBuilder.build();
  }

  private SSOClientConfig processSSOClientConfig(URL serverUrl, String providerType, EgoConfig existingConfig) {

    val clientConfigBuilder = SSOClientConfig.builder();

    val clientId =
        questionFactory
            .newDefaultSingleQuestion(
                String.class,
                format(WHAT_IS_THE_S_CLIENT_ID, providerType.toUpperCase()),
                false,
                null)
            .getAnswer();
    clientConfigBuilder.clientId(clientId);

    val clientSecret =
        questionFactory
            .newDefaultSingleQuestion(
                String.class, format(WHAT_IS_THE_S_CLIENT_SECRET, providerType), false, null)
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
      val clientSecret = generateAppSecret();
      return AppCredential.builder()
          .name(DEFAULT_DMS_APP_NAME)
          .clientId(DEFAULT_DMS_APP_CLIENT_ID)
          .clientSecret(clientSecret)
          .build();
    }
    return apiConfig.getDmsAppCredential();
  }

  private String generateAppSecret() {
    return RANDOM_GENERATOR.generateRandomAsciiString(DEFAULT_PASSWORD_LENGTH);
  }

  public enum SSOProviders {
    GOOGLE(SSOConfig::setGoogle),
    LINKEDIN(SSOConfig::setLinkedin),
    GITHUB(SSOConfig::setGithub),
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
