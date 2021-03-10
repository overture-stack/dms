package bio.overture.dms.core.model.dmsconfig;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.net.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class ElasticsearchConfig {

  public static final int DEFAULT_PORT = 9200;
  @Builder.Default private int hostPort = DEFAULT_PORT;
  @NonNull private Security security;

  URL url;
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class Security {
    private String rootPassword;
    @Builder.Default private boolean enabled = true;
  }
}
