package bio.overture.dms.core.properties.ego;

import bio.overture.dms.core.properties.DatabaseProperties;
import bio.overture.dms.core.properties.FlywayProperties;
import bio.overture.dms.core.properties.ServiceProperties;
import bio.overture.dms.core.properties.env.EnvObject;
import bio.overture.dms.core.properties.env.EnvPrefix;
import bio.overture.dms.core.properties.env.EnvVariable;
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
  @EnvObject
  private final DatabaseProperties databaseProperties;

  @NonNull
  @EnvObject
  private final FlywayProperties flywayProperties;

  @NonNull
  @EnvObject
  private final SwaggerProperties swaggerProperties;

  @NonNull
  @EnvObject
  private final RefreshTokenProperties refreshTokenProperties;

  @NonNull
  @EnvObject
  @EnvPrefix("GOOGLE_CLIENT")
  private final SSOProperties googleSSOProperties;

}
