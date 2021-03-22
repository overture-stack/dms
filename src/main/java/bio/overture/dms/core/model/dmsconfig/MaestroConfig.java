package bio.overture.dms.core.model.dmsconfig;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URL;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class MaestroConfig {
  public static final int DEFAULT_PORT = 11235;
  public static final String FILE_CENTRIC_INDEX_NAME = "file_centric_1";
  public static final String FILE_CENTRIC_ALIAS_NAME = "file_centric";
  @Builder.Default private int hostPort = DEFAULT_PORT;
  @Builder.Default private String fileCentricIndexName = FILE_CENTRIC_INDEX_NAME;
  @Builder.Default private String fileCentricAlias = FILE_CENTRIC_ALIAS_NAME;
  URL url;
}
