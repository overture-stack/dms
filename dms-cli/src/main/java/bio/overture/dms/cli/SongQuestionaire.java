package bio.overture.dms.cli;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class SongQuestionaire {

  @Question("Would you like to use kafka?")
  private boolean useKafka;

  @Question(
      value = "What should the song_id be?",
      dependentFieldNames = {Fields.useKafka})
  private String songId;
}
