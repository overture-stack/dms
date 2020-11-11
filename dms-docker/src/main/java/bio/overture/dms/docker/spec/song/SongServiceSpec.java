package bio.overture.dms.docker.spec.song;

import bio.overture.dms.docker.env.EnvVariable;
import bio.overture.dms.docker.spec.DatabaseSpec;
import bio.overture.dms.docker.spec.ServiceSpec;
import bio.overture.dms.docker.spec.FlywaySpec;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SongServiceSpec implements ServiceSpec {

  @EnvVariable("SERVER_PORT")
  private final int serverPort;

  @EnvVariable("MANAGEMENT_SERVER_PORT")
  private final int managementServerPort;

  @NonNull
  @EnvVariable("SPRING_PROFILES_ACTIVE")
  private final String springProfilesActive;

  @NonNull private final AuthServerProperties authServerProperties;

  @NonNull private final DatabaseSpec databaseSpec;

  @NonNull private final FlywaySpec flywaySpec;

  @NonNull private final KafkaProperties kafkaProperties;

  @NonNull private final IdProperties idProperties;

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
    @NonNull
    @EnvVariable("AUTH_SERVER_URL")
    private final String url;

    @NonNull
    @EnvVariable("AUTH_SERVER_TOKENNAME")
    private final String tokenName;

    @NonNull
    @EnvVariable("AUTH_SERVER_CLIENTID")
    private final String clientId;

    @NonNull
    @EnvVariable("AUTH_SERVER_CLIENTSECRET")
    private final String clientSecret;

    @NonNull private final AuthScopeProperties authScopeProperties;

    @Value
    @Builder
    public static class AuthScopeProperties {
      @NonNull
      @EnvVariable("AUTH_SERVER_SCOPE_STUDY_PREFIX")
      private final String studyPrefix;

      @NonNull
      @EnvVariable("AUTH_SERVER_SCOPE_STUDY_SUFFIX")
      private final String studySuffix;

      @NonNull
      @EnvVariable("AUTH_SERVER_SCOPE_STUDY_SYSTEM")
      private final String system;
    }
  }
}
