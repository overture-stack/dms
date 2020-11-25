package bio.overture.dms.cli.command.spec;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.util.VersionProvider;
import bio.overture.dms.infra.spec.DmsSpec;
import bio.overture.dms.infra.spec.EgoSpec;
import bio.overture.dms.infra.spec.EgoSpec.SSOClientSpec;
import bio.overture.dms.infra.spec.EgoSpec.SSOSpec;
import bio.overture.dms.infra.util.JsonProcessor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.beryx.textio.TextTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.util.concurrent.TimeUnit.HOURS;

@Component
@Slf4j
@Command(
    name = "config",
    aliases = {"co"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Interactively configure a spec" )
public class SpecConfigCommand implements Callable<Integer> {

  private final QuestionFactory questionFactory;
  private final JsonProcessor yamlProcessor;
  private final BuildProperties buildProperties;
  private final TextTerminal<?> textTerminal;

  @Autowired
  public SpecConfigCommand(@NonNull QuestionFactory questionFactory,
      @NonNull JsonProcessor yamlProcessor,
      @NonNull BuildProperties buildProperties,
      @NonNull TextTerminal<?> textTerminal ) {
    this.questionFactory =questionFactory;
    this.yamlProcessor = yamlProcessor;
    this.buildProperties = buildProperties;
    this.textTerminal = textTerminal;
  }

  @Option(names = { "--skip-answered" },
      required = false,
      description = "Skip previously answered questions, and jump to the first unanswered question")
  private boolean skipAnswered = false;

  @Option(names = { "--skip-system-check" },
      required = false,
      description = "Skip the system check")
  private boolean skipSystemCheck = false;


  @SneakyThrows
  private Path provisionSpecFile(){
    val userDir = Paths.get(System.getProperty("user.home"));
    val dmsDir = userDir.resolve(".dms");
    createDirectories(dmsDir);
    return dmsDir.resolve("spec.yaml");
  }

  @Override
  public Integer call() throws Exception {
    textTerminal.executeWithPropertiesPrefix("status", x -> x.println("Starting interactive configuration"));
    val specFile = provisionSpecFile();

    val egoSpec = buildEgoSpec();
    val dmsSpec = DmsSpec.builder()
        .version(buildProperties.getVersion())
        .ego(egoSpec)
        .build();
    yamlProcessor.writeToFile(dmsSpec, specFile);
    textTerminal.executeWithPropertiesPrefix("status", x -> x.println("Wrote spec file to "+specFile));
    return 0;
  }

  private EgoSpec buildEgoSpec(){
    val b = EgoSpec.builder();
    val hostName = questionFactory
        .newSingleQuestion(String.class, "What is the host name for the dms cluster?",
            false, null)
        .getAnswer();
    b.host(hostName);

    val apiKeyDurationDays = questionFactory.newSingleQuestion(Integer.class,
        "How many days should api keys be valid for?", true, 30)
        .getAnswer();
    b.apiTokenDurationDays(apiKeyDurationDays);

    val jwtDurationHours= questionFactory.newSingleQuestion(Integer.class,
        "How many hours should JWTs be valid for?", true, 3)
        .getAnswer();
    b.jwtDurationMS(HOURS.toMillis(jwtDurationHours));

    val refreshTokenDurationHours= questionFactory.newSingleQuestion(Integer.class,
        "How many hours should refresh tokens be valid for?", true, 12)
        .getAnswer();
    b.refreshTokenDurationMS(HOURS.toMillis(refreshTokenDurationHours));


    val apiPort= questionFactory.newSingleQuestion(Integer.class,
        "What port would you like to expose the EGO api on?", true, 9000)
        .getAnswer();
    b.apiHostPort(apiPort);

    val isSetDBPassword = questionFactory.newSingleQuestion(Boolean.class,
        "Would you like to set the database password for EGO?", false, null)
        .getAnswer();

    if (isSetDBPassword){
      val dbPassword = questionFactory.newPasswordQuestion("What should the EGO db password be?")
          .getAnswer();
      b.databasePassword(dbPassword);
    }

    val dbPort= questionFactory.newSingleQuestion(Integer.class,
        "What port would you like to expose the EGO db on?", true, 9001)
        .getAnswer();
    b.dbHostPort(dbPort);

    val egoSpec = b.build();
    egoSpec.setSso(new SSOSpec());

    val ssoProviderSelection = questionFactory.newMCQuestion(SSOProviders.class,
        "What SSO providers would you like to enable?", false, null).getAnswer();

    ssoProviderSelection.forEach(p -> {
      val clientSpecBuilder = SSOClientSpec.builder();

      val clientId = questionFactory.newSingleQuestion(String.class,
          format("What is the %s client id?", p.toString()), false, null)
          .getAnswer();
      clientSpecBuilder.clientId(clientId);

      val clientSecret = questionFactory.newSingleQuestion(String.class,
          format("What is the %s client secret?", p.toString()), false, null)
          .getAnswer();
      clientSpecBuilder.clientSecret(clientSecret);

      val preEstablishedRedirectUri = questionFactory.newSingleQuestion(String.class,
          format("What is the %s pre-established redirect url?", p.toString()), true,
          format("https://%s/api/oauth/login/%s", hostName, p.toString().toLowerCase()))
          .getAnswer();
      clientSpecBuilder.preEstablishedRedirectUri(preEstablishedRedirectUri);

      val clientSpec = clientSpecBuilder.build();

      p.setClientSpec(egoSpec.getSso(), clientSpec);
    });

    return egoSpec;

  }

  public enum SSOProviders{
    GOOGLE(SSOSpec::setGoogle),
    LINKEDIN(SSOSpec::setLinkedin),
    GITHUB(SSOSpec::setGithub),
    FACEBOOK(SSOSpec::setFacebook),
    ORCID(SSOSpec::setOrcid);

    private final BiConsumer<SSOSpec, SSOClientSpec> setter;

    SSOProviders(BiConsumer<SSOSpec, SSOClientSpec> setter) {
      this.setter = setter;
    }

    public void setClientSpec(SSOSpec s, SSOClientSpec clientSpec){
      setter.accept(s, clientSpec);
    }

  }
}

