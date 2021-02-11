package bio.overture.dms.core.model.dmsconfig;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static lombok.AccessLevel.PRIVATE;

import bio.overture.dms.core.model.enums.ClusterRunModes;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URL;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = PRIVATE)
@JsonInclude(NON_EMPTY)
public class DmsConfig {

  private final URL gatewayUrl;

  @NonNull private final ClusterRunModes clusterRunMode;

  @NonNull private final String version;

  @NonNull private final String network;

  @NonNull private final EgoConfig ego;

  @NonNull private final SongConfig song;

  @NonNull private final ScoreConfig score;

  @NonNull private final ElasticsearchConfig elasticsearch;

  @NonNull private final MaestroConfig maestro;

  @NonNull private final ArrangerConfig arranger;
}
