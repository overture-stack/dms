package bio.overture.dms.core.model.dmsconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.validation.constraints.Min;

import java.net.URL;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class ArrangerConfig {

  private ArrangerUIConfig ui;
  private ArrangerApiConfig api;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class ArrangerUIConfig {
    public static final int DEFAULT_PORT = 8080;
    @Min(value = 2000)
    @Builder.Default
      private int hostPort = 8080;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class ArrangerApiConfig {
    public static final int DEFAULT_PORT = 5050;
    @Min(value = 2000)
    @Builder.Default
    private int hostPort = DEFAULT_PORT;
    @NonNull private URL url;
  }
}


