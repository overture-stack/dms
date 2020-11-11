package bio.overture.dms.spec.model;

import bio.overture.dms.spec.model.ego.EgoServiceSpec;
import bio.overture.dms.spec.model.ego.RefreshTokenSpec;
import bio.overture.dms.spec.model.ego.SSOSpec;
import bio.overture.dms.spec.model.ego.SwaggerSpec;
import lombok.val;
import org.junit.jupiter.api.Test;

import static bio.overture.dms.spec.util.JsonUtils.readFromString;
import static bio.overture.dms.spec.util.JsonUtils.writeToString;
import static bio.overture.dms.spec.util.Tester.assertEquals;

public class DeserializationTest {

  @Test
  public void deserializationTest_Ego_Success(){
    val egoSpec = EgoServiceSpec.builder()
        .serverPort(8080)
        .springProfilesActive("grpc,auth,jwt")
        .apiTokenDurationDays(30)
        .jwtDurationMs(3*3600*1000)
        .db(DatabaseSpec.builder()
            .url("someUrl")
            .username("someUserName")
            .password("somePassword")
            .build())
        .flyway(FlywaySpec.builder()
            .enabled(true)
            .locations("someFlywayLocation")
            .build())
        .swagger(SwaggerSpec.builder()
            .baseUrl("someBaseUrl")
            .host("someHost")
            .build())
        .refreshToken(RefreshTokenSpec.builder()
            .durationMs(2*3600*1000)
            .cookieIsSecure(true)
            .domain("someDomain")
            .build())
        .googleClient(SSOSpec.builder()
            .preEstablishedRedirectUri("someGoogleUri")
            .clientId("someGoogleClientId")
            .clientSecret("someGoogleClientSecret")
            .build())
        .linkedinClient(SSOSpec.builder()
            .preEstablishedRedirectUri("someLinkedinUri")
            .clientId("someLinkedinClientId")
            .clientSecret("someLinkedinClientSecret")
            .build())
        .facebookClient(SSOSpec.builder()
            .preEstablishedRedirectUri("someFacebookUri")
            .clientId("someFacebookClientId")
            .clientSecret("someFacebookClientSecret")
            .build())
        .githubClient(SSOSpec.builder()
            .preEstablishedRedirectUri("someGithubUri")
            .clientId("someGithubClientId")
            .clientSecret("someGithubClientSecret")
            .build())
        .build();
    runDeserializerTest(egoSpec);
  }

  private static void runDeserializerTest(Object obj){
    val serialized= writeToString(obj);
    val deserialized = readFromString(serialized, obj.getClass());
    assertEquals(obj, deserialized);
  }

}
