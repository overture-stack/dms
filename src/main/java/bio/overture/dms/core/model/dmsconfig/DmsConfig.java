package bio.overture.dms.core.model.dmsconfig;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static lombok.AccessLevel.PRIVATE;

import bio.overture.dms.cli.questionnaire.DmsQuestionnaire.ClusterRunModes;
import com.fasterxml.jackson.annotation.JsonInclude;
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

  @NonNull private final ClusterRunModes clusterRunMode;

  @NonNull private final String version;

  @NonNull private final EgoConfig2 ego;

  //  @NonNull private final SongConfig song;
}
