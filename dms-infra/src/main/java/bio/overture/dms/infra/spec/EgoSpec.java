package bio.overture.dms.infra.spec;

import bio.overture.dms.core.Nullable;
import bio.overture.dms.infra.env.EnvVariable;
import bio.overture.dms.infra.properties.service.ego.SSOProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static bio.overture.dms.core.Strings.isBlank;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.Objects.isNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class EgoSpec {

  @Min(value = 1)
  @Builder.Default
  private int apiTokenDurationDays = 30;

  @Min(value = 60000L)
  @Builder.Default
  private long jwtDurationMS = 10800000L;

  @Min(value = 60000L)
  @Builder.Default
  private long refreshTokenDurationMS= 43200000L;

  @NotNull
  private SSOSpec sso;

  @Pattern(regexp = "^\\S+$")
  private String host;


  //TODO: enable parameter validation
  @Pattern(regexp = "^[A-Za-z0-9]+")
  private String databasePassword;

  public boolean isDatabasePasswordDefined(){
    return !isBlank(databasePassword);
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class SSOSpec {
    @Nullable
    private SSOClientSpec google;

    @Nullable
    private SSOClientSpec github;

    @Nullable
    private SSOClientSpec linkedin;

    @Nullable
    private SSOClientSpec facebook;

    @Nullable
    private SSOClientSpec orcid;

  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(NON_EMPTY)
  public static class SSOClientSpec {
    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    @NotBlank
    private String preEstablishedRedirectUri;
  }

}
