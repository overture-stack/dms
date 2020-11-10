package bio.overture.dms.core.spec.ego;

import bio.overture.dms.core.env.EnvVariable;
import bio.overture.dms.core.spec.DatabaseSpec;
import bio.overture.dms.core.spec.FlywaySpec;
import bio.overture.dms.core.spec.ServiceSpec;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = PRIVATE)
public class EgoServiceSpec implements ServiceSpec {

  @NonNull
  @EnvVariable("SERVER_PORT")
  private final int serverPort;

  @NonNull
  @EnvVariable("SPRING_PROFILES_ACTIVE")
  private final String springProfilesActive;

  /**
   *  Optional
   */
  @EnvVariable("APITOKEN_DURATIONDAYS")
  private final Integer apiTokenDurationDays;

  @EnvVariable("JWT_DURATIONMS")
  private final Integer jwtDurationMs;

  @NonNull
  @EnvVariable("SPRING_DATASOURCE")
  private final DatabaseSpec databaseSpec;

  @NonNull
  @EnvVariable("SPRING_FLYWAY")
  private final FlywaySpec flywaySpec;

  @NonNull
  private final SwaggerSpec swagger;

  @NonNull
  private final RefreshTokenSpec refreshToken;

  @EnvVariable("GOOGLE_CLIENT")
  private final SSOSpec googleClient;

  @EnvVariable("LINKEDIN_CLIENT")
  private final SSOSpec linkedinClient;

  @EnvVariable("GITHUB_CLIENT")
  private final SSOSpec githubClient;

  @EnvVariable("FACEBOOK_CLIENT")
  private final SSOSpec facebookClient;

}
