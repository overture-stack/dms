package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.resolveServiceConnectionInfo;
import static bio.overture.dms.compose.model.ComposeServiceResources.*;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.core.model.dmsconfig.ArrangerConfig;
import bio.overture.dms.core.model.dmsconfig.GatewayConfig;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArrangerQuestionnaire {

  private final QuestionFactory questionFactory;

  @Autowired
  public ArrangerQuestionnaire(@NonNull QuestionFactory questionFactory) {
    this.questionFactory = questionFactory;
  }

  public ArrangerConfig buildConfig(ClusterRunModes clusterRunMode, GatewayConfig gatewayConfig) {

    val info =
        resolveServiceConnectionInfo(
            clusterRunMode, gatewayConfig, questionFactory, ARRANGER_SERVER.toString(), 5050);

    val uiHostInfo =
        resolveServiceConnectionInfo(
            clusterRunMode,
            gatewayConfig,
            questionFactory,
            ARRANGER_UI.toString(),
            ArrangerConfig.ArrangerUIConfig.DEFAULT_PORT);
    return ArrangerConfig.builder()
        .api(
            ArrangerConfig.ArrangerApiConfig.builder()
                .hostPort(info.port)
                .url(info.serverUrl)
                .build())
        .ui(
            ArrangerConfig.ArrangerUIConfig.builder()
                .url(uiHostInfo.serverUrl)
                .hostPort(uiHostInfo.port)
                .build())
        .build();
  }
}
