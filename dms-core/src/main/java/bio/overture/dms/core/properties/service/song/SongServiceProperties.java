package bio.overture.dms.core.properties.service.song;

import bio.overture.dms.core.properties.service.ServiceProperties;
import bio.overture.dms.core.properties.service.env.EnvObject;
import bio.overture.dms.core.properties.service.env.EnvVariable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SongServiceProperties implements ServiceProperties {

  @NonNull private final String name;

  @EnvVariable("SERVER_PORT")
  private final int serverPort;

  @EnvVariable("MANAGEMENT_SERVER_PORT")
  private final int managementServerPort;

  @EnvVariable("SPRING_PROFILES_ACTIVE")
  @NonNull
  private final String springProfilesActive;

  @EnvObject @NonNull private final AuthServerProperties authServerProperties;

  @EnvObject @NonNull private final DatabaseProperties databaseProperties;

  @EnvObject @NonNull private final FlywayProperties flywayProperties;

  @EnvObject @NonNull private final KafkaProperties kafkaProperties;

  @EnvObject @NonNull private final IdProperties idProperties;

  @Value
  @Builder
  public static class IdProperties {
    @EnvVariable("ID_USELOCAL")
    private final boolean useLocal;
  }

  @Value
  @Builder
  public static class KafkaProperties {
    @EnvVariable("SONG_ID")
    @NonNull
    private final String songId;

    @EnvVariable("SPRING_KAFKA_BOOTSTRAPSERVERS")
    @NonNull
    private final String bootstrapServers;
  }

  @Value
  @Builder
  public static class FlywayProperties {
    @EnvVariable("SPRING_FLYWAY_ENABLED")
    private final boolean enabled;

    @EnvVariable("SPRING_FLYWAY_LOCATIONS")
    @NonNull
    private final String locations;
  }

  @Value
  @Builder
  public static class DatabaseProperties {
    @EnvVariable("SPRING_DATASOURCE_URL")
    @NonNull
    private final String url;

    @EnvVariable("SPRING_DATASOURCE_USERNAME")
    @NonNull
    private final String username;

    @EnvVariable("SPRING_DATASOURCE_PASSWORD")
    @NonNull
    private final String password;
  }

  @Value
  @Builder
  public static class ScoreProperties {
    @EnvVariable("SCORE_URL")
    @NonNull
    private final String url;

    @EnvVariable("SCORE_ACCESSTOKEN")
    @NonNull
    private final String accessToken;
  }

  @Value
  @Builder
  public static class AuthServerProperties {
    @EnvVariable("AUTH_SERVER_URL")
    @NonNull
    private final String url;

    @EnvVariable("AUTH_SERVER_TOKENNAME")
    @NonNull
    private final String tokenName;

    @EnvVariable("AUTH_SERVER_CLIENTID")
    @NonNull
    private final String clientId;

    @EnvVariable("AUTH_SERVER_CLIENTSECRET")
    @NonNull
    private final String clientSecret;

    @EnvObject @NonNull private final AuthScopeProperties authScopeProperties;

    @Value
    @Builder
    public static class AuthScopeProperties {
      @EnvVariable("AUTH_SERVER_SCOPE_STUDY_PREFIX")
      @NonNull
      private final String studyPrefix;

      @EnvVariable("AUTH_SERVER_SCOPE_STUDY_SUFFIX")
      @NonNull
      private final String studySuffix;

      @EnvVariable("AUTH_SERVER_SCOPE_STUDY_SYSTEM")
      @NonNull
      private final String system;
    }
  }
}
