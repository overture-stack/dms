package bio.overture.dms.ego;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import bio.overture.dms.core.util.ObjectSerializer;
import bio.overture.dms.ego.client.EgoClient;
import bio.overture.dms.ego.model.EgoToken;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class EgoClientTest {

  /** Constants */
  private static final EgoToken EXPECTED_EGO_TOKEN =
      EgoToken.builder()
          .accessToken("someJwt")
          .expiresIn(9999999)
          .scope("*.WRITE")
          .tokenType("Bearer")
          .build();

  private static final String TEST_CLIENT_ID = "someClientId";
  private static final String TEST_CLIENT_SECRET = "someClientSecret";

  /** Dependencies */
  @Autowired private ObjectSerializer jsonSerializer;

  @Autowired private EgoClientFactory egoClientFactory;
  @Autowired private Environment environment;

  /** State */
  private EgoClient egoClient;

  private boolean initialized;

  private String getMockServerUrl() {
    val port = Integer.parseInt(environment.getProperty("wiremock.server.port"));
    return "http://localhost:" + port;
  }

  @BeforeEach
  public synchronized void beforeTest() {
    if (!initialized) {
      this.egoClient = egoClientFactory.buildNoAuthEgoClient(getMockServerUrl());
      initialized = true;
    }
    reset();
  }

  @Test
  public void testPostAccessToken() {
    setupPostAccessToken(TEST_CLIENT_ID, TEST_CLIENT_SECRET);
    val actualEgoToken = egoClient.postAccessToken(TEST_CLIENT_ID, TEST_CLIENT_SECRET);
    assertEquals(EXPECTED_EGO_TOKEN, actualEgoToken);
  }

  @Test
  public void testGetPublicKey() {
    val expectedPublicKey = "somePublicKeyString";
    setupGetPublicKey(expectedPublicKey);
    val actualPublicKey = egoClient.getPublicKey();
    assertEquals(expectedPublicKey, actualPublicKey);
  }

  private void setupGetPublicKey(String expectedPublicKey) {
    stubFor(
        get(urlEqualTo("/oauth/token/public_key"))
            .willReturn(aResponse().withStatus(200).withBody(expectedPublicKey)));
  }

  private void setupPostAccessToken(String clientId, String clientSecret) {
    stubFor(
        post(urlEqualTo(
                format(
                    "/oauth/token?client_id=%s&client_secret=%s&grant_type=client_credentials",
                    clientId, clientSecret)))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(jsonSerializer.serializeValue(EXPECTED_EGO_TOKEN))));
  }
}
