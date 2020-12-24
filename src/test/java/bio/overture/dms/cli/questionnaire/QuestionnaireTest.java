package bio.overture.dms.cli.questionnaire;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import bio.overture.dms.cli.question.QuestionFactory;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.beryx.textio.TextIO;
import org.beryx.textio.mock.MockTextTerminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class QuestionnaireTest {

  /** State */
  private MockTextTerminal mockTextTerminal;

  private EgoQuestionnaire egoQuestionnaire;

  @BeforeEach
  public void beforeTest() {
    this.mockTextTerminal = new MockTextTerminal();
    this.egoQuestionnaire = new EgoQuestionnaire(new QuestionFactory(new TextIO(mockTextTerminal)));
  }

  @Test
  public void egoQuestionnaire_normal_success() {
    val inputs =
        List.of(
            "dms.example.org",
            "30",
            "3",
            "12",
            "9000",
            "N",
            "9001",
            "1",
            "googleClientId",
            "googleClientSecret",
            "some.redirect.google.example.org",
            "dms",
            "30");

    this.mockTextTerminal.getInputs().addAll(inputs);
    val egoConfig = this.egoQuestionnaire.buildEgoConfig();

    assertEquals("dms.example.org", egoConfig.getHost());
    assertEquals(30, egoConfig.getApiTokenDurationDays());
    assertEquals(Duration.of(3, HOURS).toMillis(), egoConfig.getJwtDurationMS());
    assertEquals(Duration.of(12, HOURS).toMillis(), egoConfig.getRefreshTokenDurationMS());
    assertEquals(9000, egoConfig.getApiHostPort());
    assertNull(egoConfig.getDatabasePassword());
    assertEquals(9001, egoConfig.getDbHostPort());
    assertNull(egoConfig.getSso().getGithub());
    assertNull(egoConfig.getSso().getFacebook());
    assertNull(egoConfig.getSso().getOrcid());
    assertNull(egoConfig.getSso().getLinkedin());
    assertNotNull(egoConfig.getSso().getGoogle());
    val google = egoConfig.getSso().getGoogle();
    assertEquals("googleClientId", google.getClientId());
    assertEquals("googleClientSecret", google.getClientSecret());
    assertEquals("some.redirect.google.example.org", google.getPreEstablishedRedirectUri());
    assertEquals("dms", egoConfig.getDmsAppCredentials().getName());
    assertEquals("dms", egoConfig.getDmsAppCredentials().getClientId());
    assertEquals(30, egoConfig.getDmsAppCredentials().getClientSecret().length());
  }
}
