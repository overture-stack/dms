package bio.overture.dms.core.properties.service;

import bio.overture.dms.core.properties.service.song.SongServiceProperties;
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
            .name("myname")
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
                SongServiceProperties.DatabaseProperties.builder()
                    .password("mydbpassword1")
                    .url("mydburl")
                    .username("mydbusername1")
                    .build())
            .flywayProperties(
                SongServiceProperties.FlywayProperties.builder()
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
