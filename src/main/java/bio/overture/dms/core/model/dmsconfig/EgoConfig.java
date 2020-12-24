package bio.overture.dms.core.model.dmsconfig;

import static bio.overture.dms.core.util.Strings.isDefined;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import bio.overture.dms.core.util.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class EgoConfig {

  @Min(value = 1)
  @Builder.Default
  private int apiTokenDurationDays = 30;

  @Min(value = 60000L)
  @Builder.Default
  private long jwtDurationMS = 10800000L;

  @Min(value = 60000L)
  @Builder.Default
  private long refreshTokenDurationMS = 43200000L;

  @Min(value = 2000)
  @Builder.Default
  private int apiHostPort = 9000;

  @NotNull private EgoConfig.SSOConfig sso;

  @Pattern(regexp = "^\\S+$")
  private String host;

  // TODO: enable parameter validation
  @Pattern(regexp = "^[A-Za-z0-9]+")
  private String databasePassword;

  @Min(value = 2000)
  @Builder.Default
  private int dbHostPort = 9001;

  @JsonIgnore
  public boolean isDatabasePasswordDefined() {
    return isDefined(databasePassword);
  }

  private DmsAppCredentials dmsAppCredentials;

  /**
   * Represents the ego application credentials, that is used by this program to call any endpoint
   * on the ego service. This implies, the app credential is of role ADMIN
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class DmsAppCredentials {
    private String name;
    private String clientId;
    private String clientSecret;

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
}
