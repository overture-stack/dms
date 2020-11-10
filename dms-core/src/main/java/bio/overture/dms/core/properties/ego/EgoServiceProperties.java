package bio.overture.dms.core.properties.ego;

import bio.overture.dms.core.properties.DatabaseSpec;
import bio.overture.dms.core.properties.FlywaySpec;
import bio.overture.dms.core.properties.ServiceProperties;
import bio.overture.dms.core.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class EgoServiceProperties implements ServiceProperties {

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

  @NonNull
  @EnvVariable("GOOGLE_CLIENT")
  private final SSOSpec googleClient;

  @NonNull
  @EnvVariable("LINKEDIN_CLIENT")
  private final SSOSpec linkedinClient;

  @NonNull
  @EnvVariable("GITHUB_CLIENT")
  private final SSOSpec githubClient;

  @NonNull
  @EnvVariable("FACEBOOK_CLIENT")
  private final SSOSpec facebookClient;

}
