package bio.overture.dms.cli.command.config;

import static bio.overture.dms.cli.model.enums.Constants.CONFIG_FILE_NAME;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.util.concurrent.TimeUnit.HOURS;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.util.VersionProvider;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.SSOClientConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig.SSOConfig;
import bio.overture.dms.core.util.ObjectSerializer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Slf4j
@Command(
    name = "build",
    aliases = {"bu"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Interactively build a configuration")
public class ConfigBuildCommand implements Callable<Integer> {

  private final QuestionFactory questionFactory;
  private final ObjectSerializer yamlSerializer;
  private final BuildProperties buildProperties;
  private final Terminal terminal;

  @Autowired
  public ConfigBuildCommand(
      @NonNull QuestionFactory questionFactory,
      @NonNull ObjectSerializer yamlSerializer,
      @NonNull BuildProperties buildProperties,
      @NonNull Terminal terminal) {
    this.questionFactory = questionFactory;
    this.yamlSerializer = yamlSerializer;
    this.buildProperties = buildProperties;
    this.terminal = terminal;
  }

  @Option(
      names = {"--skip-answered"},
      required = false,
      description = "Skip previously answered questions, and jump to the first unanswered question")
  private boolean skipAnswered = false;

  @Option(
      names = {"--skip-system-check"},
      required = false,
      description = "Skip the system check")
  private boolean skipSystemCheck = false;

  @SneakyThrows
  private Path provisionConfigFile() {
    val userDir = Paths.get(System.getProperty("user.home"));
    val dmsDir = userDir.resolve(".dms");
    createDirectories(dmsDir);
    return dmsDir.resolve(CONFIG_FILE_NAME);
  }

  @Override
  public Integer call() throws Exception {
    terminal.printStatusLn("Starting interactive configuration");
    val configFile = provisionConfigFile();

    val egoConfig = buildEgoConfig();
    val dmsConfig =
        DmsConfig.builder().version(buildProperties.getVersion()).ego(egoConfig).build();
    yamlSerializer.serializeToFile(dmsConfig, configFile.toFile());
    terminal.printStatusLn("Wrote config file to %s", configFile);
    return 0;
  }

  private EgoConfig buildEgoConfig() {
    val b = EgoConfig.builder();
    val hostName =
        questionFactory
            .newDefaultSingleQuestion(
                String.class, "What is the host name for the dms cluster?", false, null)
            .getAnswer();
    b.host(hostName);

    val apiKeyDurationDays =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "How many days should api keys be valid for?", true, 30)
            .getAnswer();
    b.apiTokenDurationDays(apiKeyDurationDays);

    val jwtDurationHours =
        questionFactory
            .newDefaultSingleQuestion(
                Integer.class, "How many hours should JWTs be valid for?", true, 3)
            .getAnswer();
    b.jwtDurationMS(HOURS.toMillis(jwtDurationHours));

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
    egoConfig.setSso(new SSOConfig());

    val ssoProviderSelection =
        questionFactory
            .newMCQuestion(
                SSOProviders.class, "What SSO providers would you like to enable?", false, null)
            .getAnswer();

    ssoProviderSelection.forEach(
        p -> {
          val clientConfigBuilder = SSOClientConfig.builder();

          val clientId =
              questionFactory
                  .newDefaultSingleQuestion(
                      String.class, format("What is the %s client id?", p.toString()), false, null)
                  .getAnswer();
          clientConfigBuilder.clientId(clientId);

          val clientSecret =
              questionFactory
                  .newDefaultSingleQuestion(
                      String.class,
                      format("What is the %s client secret?", p.toString()),
                      false,
                      null)
                  .getAnswer();
          clientConfigBuilder.clientSecret(clientSecret);

          val preEstablishedRedirectUri =
              questionFactory
                  .newDefaultSingleQuestion(
                      String.class,
                      format("What is the %s pre-established redirect url?", p.toString()),
                      true,
                      format("https://%s/api/oauth/login/%s", hostName, p.toString().toLowerCase()))
                  .getAnswer();
          clientConfigBuilder.preEstablishedRedirectUri(preEstablishedRedirectUri);

          val clientConfig = clientConfigBuilder.build();

          p.setClientConfig(egoConfig.getSso(), clientConfig);
        });

    return egoConfig;
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
