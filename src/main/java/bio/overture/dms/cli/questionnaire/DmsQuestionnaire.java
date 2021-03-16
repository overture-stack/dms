package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.core.model.enums.ClusterRunModes.SERVER;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.compose.properties.ComposeProperties;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
import bio.overture.dms.core.model.dmsconfig.HealthCheckConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class DmsQuestionnaire {

  private final QuestionFactory questionFactory;
  private final BuildProperties buildProperties;
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
      @NonNull BuildProperties buildProperties,
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
    this.buildProperties = buildProperties;
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

  @SneakyThrows
  public DmsConfig buildDmsConfig() {
    val clusterRunMode =
        questionFactory
            .newOneHotQuestion(
                ClusterRunModes.class, "Select the cluster mode to configure: ", false, null)
            .getAnswer();

    GatewayConfig gatewayConfig;
    URL dmsGatewayUrl;
    int gatewayPort;
    String sslPath = "/etc/ssl/dms";
    if (clusterRunMode == SERVER) {
      dmsGatewayUrl =
          questionFactory
              .newUrlSingleQuestion("What is the base DMS Gateway URL (example: https://dms.cancercollaboratory.org)?",
                  false,
                  null
              ).getAnswer();

      if (dmsGatewayUrl.getPort() <= 0) {
        dmsGatewayUrl = new URI("https", null, dmsGatewayUrl.getHost(),
            443, null, null, null).toURL();
      }

       sslPath =
          questionFactory
              .newDefaultSingleQuestion(String.class,"What is the absolute path for the SSL certificate ?",
                  false,
                  "/etc/letsencrypt/live/" + dmsGatewayUrl.getHost() + "/"
              ).getAnswer();

      gatewayPort = 443;
    } else {
      gatewayPort =
          questionFactory
              .newDefaultSingleQuestion(
                  Integer.class,
                  "What port will the gateway be exposed on?",
                  true,
                  80
              ).getAnswer();
      dmsGatewayUrl = new URI("http", null, "localhost",
          gatewayPort, null, null, null).toURL();
    }

    gatewayConfig = GatewayConfig.builder()
        .hostPort(gatewayPort)
        .url(dmsGatewayUrl)
        .sslDir(sslPath)
        .build();

    printHeader("EGO");
    val egoConfig = egoQuestionnaire.buildEgoConfig(clusterRunMode, gatewayConfig);
    printHeader("SONG");
    val songConfig = songQuestionnaire.buildSongConfig(clusterRunMode, gatewayConfig);
    printHeader("SCORE");
    val scoreConfig = scoreQuestionnaire.buildScoreConfig(clusterRunMode, gatewayConfig);
    printHeader("ELASTICSEARCH");
    val elasticConfig = elasticsearchQuestionnaire.buildConfig(clusterRunMode, gatewayConfig);
    printHeader("MAESTRO");
    val maestroConfig = maestroQuestionnaire.buildConfig(clusterRunMode, gatewayConfig);
    // there are no questions for arranger
    //printHeader("ARRANGER");
    val arrangerConfig = arrangerQuestionnaire.buildConfig(clusterRunMode, gatewayConfig);
    printHeader("DMS UI");
    // we pass maestro's config to read the alias name to be used
    // in case the user changed the default.
    val dmsUIConfig = dmsUIQuestionnaire.buildConfig(maestroConfig, clusterRunMode, gatewayConfig);

    return DmsConfig.builder()
        .gateway(gatewayConfig)
        .clusterRunMode(clusterRunMode)
        .healthCheck(HealthCheckConfig.builder()
            .build())
        .version(buildProperties.getVersion())
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

  @SneakyThrows
  static URL createLocalhostUrl(int port) {
    return new URL("http://localhost:" + port);
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
  static ServiceUrlInfo resolveServiceConnectionInfo(ClusterRunModes clusterRunMode,
                                                                GatewayConfig gatewayConfig,
                                                                QuestionFactory questionFactory,
                                                                String serviceName,
                                                                int defaultApiPort) {
    URL serverUrl;
    int apiPort = defaultApiPort;
    if (gatewayConfig.isPathBased()) {
      serverUrl = gatewayConfig.getUrl().toURI().resolve("/" + serviceName).toURL();
    } else {
      if (clusterRunMode == SERVER) {
        serverUrl = new URL("http://" + serviceName + "." + gatewayConfig.getUrl().getHost());
      } else {
        apiPort =
            questionFactory
                .newDefaultSingleQuestion(
                    Integer.class, "What port would you like to expose " + serviceName + " on?", true, defaultApiPort)
                .getAnswer();
        serverUrl = createLocalhostUrl(apiPort);
      }
    }
    val r = new ServiceUrlInfo();
    r.port = apiPort;
    r.serverUrl = serverUrl;
    return r;
  }
}
