package bio.overture.dms.properties.ego;

import static lombok.AccessLevel.PRIVATE;

import bio.overture.dms.env.EnvVariable;
import bio.overture.dms.properties.FlywayProperties;
import bio.overture.dms.properties.ServiceProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = PRIVATE)
public class EgoApiServiceProperties implements ServiceProperties {

  @NonNull
  @EnvVariable("SERVER_PORT")
  private final Integer serverPort;

  @NonNull
  @EnvVariable("SPRING_PROFILES_ACTIVE")
  private final String springProfilesActive;

  /** Optional */
  @EnvVariable("APITOKEN_DURATIONDAYS")
  private final Integer apiTokenDurationDays;

  @EnvVariable("JWT_DURATIONMS")
  private final Long jwtDurationMs;

  @NonNull
  @EnvVariable("SPRING_DATASOURCE")
  private final ClientDatabaseProperties db;

  @NonNull
  @EnvVariable("SPRING_FLYWAY")
  private final FlywayProperties flyway;

  @NonNull private final SwaggerProperties swagger;

  @NonNull private final RefreshTokenProperties refreshToken;

  @EnvVariable("GOOGLE_CLIENT")
  private final SSOProperties googleClient;

  @EnvVariable("LINKEDIN_CLIENT")
  private final SSOProperties linkedinClient;

  @EnvVariable("GITHUB_CLIENT")
  private final SSOProperties githubClient;

  @EnvVariable("FACEBOOK_CLIENT")
  private final SSOProperties facebookClient;
}
