package bio.overture.dms.core.model.dmsconfig;

import static bio.overture.dms.core.util.Strings.isDefined;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URL;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class ScoreConfig {

  @NotNull private ScoreS3Config s3;
  @NotNull private ScoreApiConfig api;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class ScoreS3Config {

    @NotNull private URL url;
    @NotBlank private String accessKey;
    @NotBlank private String secretKey;

    // These are optional
    private boolean useMinio;
    private Integer hostPort;
    private String s3Region;

    public boolean isS3RegionDefined(){
      return isDefined(s3Region);
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class ScoreApiConfig {

    @NotBlank private URL url;

    private String stateBucket;
    private String objectBucket;

    @Min(value = 2000)
    @Builder.Default
    private int hostPort = 9020;

    private AppCredential scoreAppCredential;
  }
}
