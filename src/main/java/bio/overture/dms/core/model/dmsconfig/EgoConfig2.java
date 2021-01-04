package bio.overture.dms.core.model.dmsconfig;

import bio.overture.dms.core.util.Nullable;
import bio.overture.dms.ego.client.EgoClient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.net.URL;

import static bio.overture.dms.core.util.Strings.isDefined;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class EgoConfig2 {

  @NotNull private EgoApiConfig api;
  @NotNull private EgoDbConfig db;
  @NotNull private EgoUiConfig ui;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class EgoApiConfig {
    @Min(value = 1)
    @Builder.Default
    private int tokenDurationDays = 30;

    @NotNull private JwtConfig jwt;

    @Min(value = 60000L)
    @Builder.Default
    private long refreshTokenDurationMS = 43200000L;

    @Min(value = 2000)
    @Builder.Default
    private int hostPort = 9000;

    @NotNull private EgoConfig2.SSOConfig sso;

    @NotNull private URL url;

    private AppCredentials dmsAppCredentials;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class EgoDbConfig {

    // TODO: enable parameter validation
    @Pattern(regexp = "^[A-Za-z0-9]+")
    private String databasePassword;

    @Min(value = 2000)
    @Builder.Default
    private int hostPort = 9001;

    @JsonIgnore
    public boolean isDatabasePasswordDefined() {
      return isDefined(databasePassword);
    }

  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class EgoUiConfig {
    @NotNull private URL url;
    private AppCredentials uiAppCredentials;
  }


  /**
   * Represents the ego application credentials, that is used by this program to call any endpoint
   * on the ego service. This implies, the app credential is of role ADMIN
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class AppCredentials {
    private String name;
    private String clientId;
    private String clientSecret;
    private String redirectUri;

    @JsonIgnore
    public boolean isSecretDefined() {
      return isDefined(clientSecret);
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class SSOConfig {
    @Nullable private SSOClientConfig google;

    @Nullable private SSOClientConfig github;

    @Nullable private SSOClientConfig linkedin;

    @Nullable private SSOClientConfig facebook;

    @Nullable private SSOClientConfig orcid;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class SSOClientConfig {
    @NotBlank private String clientId;

    @NotBlank private String clientSecret;

    @NotBlank private String preEstablishedRedirectUri;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class JwtConfig {
    private static final JwtDuration DEFAULT_JWT_DURATION = new JwtDuration(10800000L);

    @NotNull @Builder.Default private JwtDuration user = DEFAULT_JWT_DURATION;

    @NotNull @Builder.Default private JwtDuration app = DEFAULT_JWT_DURATION;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(NON_EMPTY)
    public static class JwtDuration {
      @Min(1)
      private long durationMs;
    }
  }
}
