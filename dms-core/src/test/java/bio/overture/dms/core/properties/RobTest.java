package bio.overture.dms.core.properties;

import bio.overture.dms.core.properties.FlywayProperties;
import bio.overture.dms.core.properties.song.SongServiceProperties;
import java.lang.reflect.InvocationTargetException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

@Slf4j
public class RobTest {

  @Test
  public void testRob()
      throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    val prop =
        SongServiceProperties.builder()
            .authServerProperties(
                SongServiceProperties.AuthServerProperties.builder()
                    .clientId("myclientId")
                    .clientSecret("myclientSecret")
                    .url("myauthurl")
                    .tokenName("mytokenName")
                    .authScopeProperties(
                        SongServiceProperties.AuthServerProperties.AuthScopeProperties.builder()
                            .system("mysystem")
                            .studyPrefix("mystudyprefix")
                            .studySuffix("mystudysuffix")
                            .build())
                    .build())
            .databaseProperties(
                DatabaseProperties.builder()
                    .password("mydbpassword1")
                    .url("mydburl")
                    .username("mydbusername1")
                    .build())
            .flywayProperties(
                FlywayProperties.builder()
                    .enabled(true)
                    .locations("somelocation")
                    .build())
            .idProperties(SongServiceProperties.IdProperties.builder().useLocal(true).build())
            .kafkaProperties(
                SongServiceProperties.KafkaProperties.builder()
                    .bootstrapServers("somebootstrap")
                    .songId("somesongid")
                    .build())
            .managementServerPort(9999)
            .serverPort(8888)
            .springProfilesActive("kafka,some,profile")
            .build();

    val out = prop.getEnvironment();

    log.info("sdf");
  }
}
