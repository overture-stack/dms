package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.model.Constants.GATEWAY.*;
import static bio.overture.dms.cli.model.Constants.GuidesURLS.*;
import static bio.overture.dms.core.model.enums.ClusterRunModes.SERVER;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.compose.properties.ComposeProperties;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
import bio.overture.dms.core.model.dmsconfig.HealthCheckConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import bio.overture.dms.core.util.Tuple;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DmsQuestionnaire {

  private final QuestionFactory questionFactory;
  private final EgoQuestionnaire egoQuestionnaire;
  private final ComposeProperties composeProperties;
  private final SongQuestionnaire songQuestionnaire;
  private final ScoreQuestionnaire scoreQuestionnaire;
  private final ElasticsearchQuestionnaire elasticsearchQuestionnaire;
  private final MaestroQuestionnaire maestroQuestionnaire;
  private final ArrangerQuestionnaire arrangerQuestionnaire;
  private final DmsUIQuestionnaire dmsUIQuestionnaire;

  private final Terminal terminal;

  @Autowired
  public DmsQuestionnaire(
      @NonNull QuestionFactory questionFactory,
      @NonNull EgoQuestionnaire egoQuestionnaire,
      @NonNull ComposeProperties composeProperties,
      @NonNull SongQuestionnaire songQuestionnaire,
      @NonNull ScoreQuestionnaire scoreQuestionnaire,
      @NonNull ElasticsearchQuestionnaire elasticsearchQuestionnaire,
      @NonNull MaestroQuestionnaire maestroQuestionnaire,
      @NonNull ArrangerQuestionnaire arrangerQuestionnaire,
      @NonNull DmsUIQuestionnaire dmsUIQuestionnaire,
      @NonNull Terminal terminal) {
    this.questionFactory = questionFactory;
    this.egoQuestionnaire = egoQuestionnaire;
    this.composeProperties = composeProperties;
    this.songQuestionnaire = songQuestionnaire;
    this.scoreQuestionnaire = scoreQuestionnaire;
    this.elasticsearchQuestionnaire = elasticsearchQuestionnaire;
    this.maestroQuestionnaire = maestroQuestionnaire;
    this.arrangerQuestionnaire = arrangerQuestionnaire;
    this.dmsUIQuestionnaire = dmsUIQuestionnaire;
    this.terminal = terminal;
  }


  public DmsConfig buildDmsConfig(@NonNull DmsConfig existingConfig) {
    GatewayConfig gatewayConfig;
    ClusterRunModes clusterRunMode;

    if (existingConfig.getClusterRunMode() == null) {
      val clusterModeResult = configureClusterMode();
      gatewayConfig = clusterModeResult.x;
      clusterRunMode = clusterModeResult.y;
    } else {
      gatewayConfig = existingConfig.getGateway();
      clusterRunMode = existingConfig.getClusterRunMode();
    }

    printHeader("EGO");
    terminal.println(GUIDE_EGO);
    val existingEgoConfig = existingConfig.getEgo();
    val egoConfig = egoQuestionnaire.buildEgoConfig(clusterRunMode, gatewayConfig, existingEgoConfig);
    printHeader("SONG");
    terminal.println(GUIDE_SONG);
    val songConfig = songQuestionnaire.buildSongConfig(clusterRunMode, gatewayConfig);
    printHeader("SCORE");
    terminal.println(GUIDE_SCORE);
    val scoreConfig = scoreQuestionnaire.buildScoreConfig(clusterRunMode, gatewayConfig);
    printHeader("ELASTICSEARCH");
    terminal.println(GUIDE_ES);
    val elasticConfig = elasticsearchQuestionnaire.buildConfig(clusterRunMode, gatewayConfig);
    printHeader("MAESTRO");
    terminal.println(GUIDE_MAESTRO);
    val maestroConfig = maestroQuestionnaire.buildConfig(clusterRunMode, gatewayConfig);
    val arrangerConfig = arrangerQuestionnaire.buildConfig(clusterRunMode, gatewayConfig);
    printHeader("DMS UI");
    // we pass maestro's config to read the alias name to be used
    // in case the user changed the default.
    terminal.println(GUIDE_DMSUI);
    val dmsUIConfig =
        dmsUIQuestionnaire.buildConfig(maestroConfig, clusterRunMode, gatewayConfig, egoConfig);

    return DmsConfig.builder()
        .gateway(gatewayConfig)
        .clusterRunMode(clusterRunMode)
        .healthCheck(HealthCheckConfig.builder().build())
        .version(existingConfig.getVersion())
        .network(composeProperties.getNetwork())
        .ego(egoConfig)
        .song(songConfig)
        .score(scoreConfig)
        .elasticsearch(elasticConfig)
        .maestro(maestroConfig)
        .dmsUI(dmsUIConfig)
        .arranger(arrangerConfig)
        .build();
  }

  private Tuple<GatewayConfig, ClusterRunModes> configureClusterMode() throws MalformedURLException, URISyntaxException {
    printHeader("CLUSTER MODE & GATEWAY");
    terminal.println(DEPLOYMENT_MODE);
    val clusterRunMode =
        questionFactory
            .newOneHotQuestion(
                ClusterRunModes.class, CLUSTER_MODE_TO_CONFIGURE_AND_DEPLOY, false, null)
            .getAnswer();

    GatewayConfig gatewayConfig;
    URL dmsGatewayUrl;
    int gatewayPort;
    String sslPath = "/etc/ssl/dms";
    if (clusterRunMode == SERVER) {
      dmsGatewayUrl =
          questionFactory.newUrlSingleQuestion(GATEWAY_BASE_URL, false, null).getAnswer();

      if (dmsGatewayUrl.getPort() <= 0) {
        dmsGatewayUrl =
            new URI("https", null, dmsGatewayUrl.getHost(), 443, null, null, null).toURL();
      }

      sslPath =
          questionFactory
              .newDefaultSingleQuestion(String.class, SSL_CERT_BASE_PATH, true, "/etc/letsencrypt/")
              .getAnswer();

      gatewayPort = 443;
    } else {
      gatewayPort =
          questionFactory
              .newDefaultSingleQuestion(Integer.class, GATEWAY_PORT, true, 80)
              .getAnswer();

      dmsGatewayUrl = new URI("http", null, "localhost", gatewayPort, null, null, null).toURL();
    }

    gatewayConfig =
        GatewayConfig.builder().hostPort(gatewayPort).url(dmsGatewayUrl).sslDir(sslPath).build();

    return new Tuple<>(gatewayConfig, clusterRunMode);
  }

  @SneakyThrows
  static URL createLocalhostUrl(int port) {
    return new URL("http://localhost:" + port);
  }

  public static <T> T resolveDefault(T existingValue, T defaultVal) {
    if (existingValue == null) {
      return defaultVal;
    }
    return existingValue;
  }

  private void printHeader(@NonNull String title) {
    val line = "===============";
    terminal.println();
    terminal.println(line + "\n" + title + "\n" + line);
  }

  static class ServiceUrlInfo {
    public URL serverUrl;
    public int port;
  }

  @SneakyThrows
  static ServiceUrlInfo resolveServiceConnectionInfo(
      GatewayConfig gatewayConfig,
      String serviceName,
      int defaultApiPort) {
    URL serverUrl;
    serverUrl = gatewayConfig.getUrl().toURI().resolve("/" + serviceName).toURL();
    val r = new ServiceUrlInfo();
    r.port = defaultApiPort;
    r.serverUrl = serverUrl;
    return r;
  }
}
