package bio.overture.dms.infra.service.properties;

import static bio.overture.dms.core.JsonUtils.readFromString;
import static bio.overture.dms.core.JsonUtils.writeToString;

import bio.overture.dms.infra.properties.service.FlywayProperties;
import bio.overture.dms.infra.properties.service.ego.ClientDatabaseProperties;
import bio.overture.dms.infra.properties.service.ego.EgoApiServiceProperties;
import bio.overture.dms.infra.properties.service.ego.RefreshTokenProperties;
import bio.overture.dms.infra.properties.service.ego.SSOProperties;
import bio.overture.dms.infra.properties.service.ego.SwaggerProperties;
import bio.overture.dms.infra.spec.EgoSpec;
import bio.overture.dms.infra.spec.EgoSpec.SSOClientSpec;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class DeserializationTest {

  @Test
  public void testRob() {
    val egoSpec =
        EgoSpec.builder()
            .host("https://ego.example.com")
            .apiTokenDurationDays(30)
            .jwtDurationMS(100000)
            .refreshTokenDurationMS(300000)
            .sso(
                EgoSpec.SSOSpec.builder()
                    .google(
                        SSOClientSpec.builder()
                            .clientId("someGoogleClientId")
                            .clientSecret("someGoogleClientSecret")
                            .preEstablishedRedirectUri("https://google.example.com")
                            .build())
                    .build())
            .build();
  }

  @Test
  public void deserializationTest_Ego_Success() {
    val egoSpec =
        EgoApiServiceProperties.builder()
            .serverPort(8080)
            .springProfilesActive("grpc,auth,jwt")
            .apiTokenDurationDays(30)
            .jwtDurationMs(3 * 3600 * 1000L)
            .db(
                ClientDatabaseProperties.builder()
                    .url("someUrl")
                    .username("someUserName")
                    .password("somePassword")
                    .build())
            .flyway(
                FlywayProperties.builder().enabled(true).locations("someFlywayLocation").build())
            .swagger(SwaggerProperties.builder().baseUrl("someBaseUrl").host("someHost").build())
            .refreshToken(
                RefreshTokenProperties.builder()
                    .durationMs(2 * 3600 * 1000L)
                    .cookieIsSecure(true)
                    .domain("someDomain")
                    .build())
            .googleClient(
                SSOProperties.builder()
                    .preEstablishedRedirectUri("someGoogleUri")
                    .clientId("someGoogleClientId")
                    .clientSecret("someGoogleClientSecret")
                    .build())
            .linkedinClient(
                SSOProperties.builder()
                    .preEstablishedRedirectUri("someLinkedinUri")
                    .clientId("someLinkedinClientId")
                    .clientSecret("someLinkedinClientSecret")
                    .build())
            .facebookClient(
                SSOProperties.builder()
                    .preEstablishedRedirectUri("someFacebookUri")
                    .clientId("someFacebookClientId")
                    .clientSecret("someFacebookClientSecret")
                    .build())
            .githubClient(
                SSOProperties.builder()
                    .preEstablishedRedirectUri("someGithubUri")
                    .clientId("someGithubClientId")
                    .clientSecret("someGithubClientSecret")
                    .build())
            .build();
    runDeserializerTest(egoSpec);
  }

  private static void runDeserializerTest(Object obj) {
    val serialized = writeToString(obj);
    val deserialized = readFromString(serialized, obj.getClass());
    Assertions.assertEquals(obj, deserialized);
  }
}
