package bio.overture.dms.core.model.dmsconfig;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.validation.constraints.Min;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class DmsUIConfig {

  @Min(value = 2000)
  @Builder.Default
  private int hostPort = 8000;

  private ArrangerProjectConfig projectConfig;

  public static final int DEFAULT_PORT = 8000;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class ArrangerProjectConfig {
    public static final String DEFAULT_PROJECT_ID = "file";
    public static final String DEFAULT_PROJECT_NAME = "file";
    public static final String DEFAULT_INDEX_ALIAS = "file_centric";
    @NonNull @Builder.Default private String id = DEFAULT_PROJECT_ID;
    @NonNull @Builder.Default private String name = DEFAULT_PROJECT_NAME;
    @NonNull @Builder.Default private String indexAlias = DEFAULT_INDEX_ALIAS;
  }
}