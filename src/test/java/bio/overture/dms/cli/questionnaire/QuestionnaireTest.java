package bio.overture.dms.cli.questionnaire;

import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.ClusterRunModes.LOCAL;
import static bio.overture.dms.cli.questionnaire.DmsQuestionnaire.ClusterRunModes.PRODUCTION;
import static bio.overture.dms.util.TestTextTerminal.createTestTextTerminal;
import static bio.overture.dms.util.Tester.handleCall;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import bio.overture.dms.cli.question.QuestionFactory;
import bio.overture.dms.util.TestTextTerminal;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.beryx.textio.TextIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class QuestionnaireTest {

  /** State */
  private TestTextTerminal testTextTerminal;

  private EgoQuestionnaire egoQuestionnaire;

  @BeforeEach
  public void beforeTest() {
    this.testTextTerminal = createTestTextTerminal();
    this.egoQuestionnaire = new EgoQuestionnaire(new QuestionFactory(new TextIO(testTextTerminal)));
  }

  @Test
  @SneakyThrows
  public void egoQuestionnaire_productionMode_success() {
    val inputs =
        List.of(
            "30",
            "3",
            "4",
            "12",
            "9000",
            "https://dms.example.org:9933/hi/there",
            "1",
            "googleClientId",
            "googleClientSecret",
            "some.redirect.google.example.org",
            "dms",
            "30",
            "N",
            "9001",
            "9002",
            "https://dms.example.org:9933/hi/there/ui");

    this.testTextTerminal.getInputs().addAll(inputs);
    log.info("sdfsdf");
    val egoConfig =
        handleCall(
            () -> this.egoQuestionnaire.buildEgoConfig(PRODUCTION),
            Throwable.class,
            e -> {
              log.error(e.getMessage());
              log.info("Terminal Output: " + testTextTerminal.getOutputAndReset(false));
            });

    assertEquals(30, egoConfig.getApi().getTokenDurationDays());
    assertEquals(
        Duration.of(3, HOURS).toMillis(), egoConfig.getApi().getJwt().getUser().getDurationMs());
    assertEquals(
        Duration.of(4, HOURS).toMillis(), egoConfig.getApi().getJwt().getApp().getDurationMs());
    assertEquals(Duration.of(12, HOURS).toMillis(), egoConfig.getApi().getRefreshTokenDurationMS());
    assertEquals(9000, egoConfig.getApi().getHostPort());
    assertEquals(new URL("https://dms.example.org:9933/hi/there"), egoConfig.getApi().getUrl());
    assertNull(egoConfig.getDb().getDatabasePassword());
    assertEquals(9001, egoConfig.getDb().getHostPort());
    assertNull(egoConfig.getApi().getSso().getGithub());
    assertNull(egoConfig.getApi().getSso().getFacebook());
    assertNull(egoConfig.getApi().getSso().getOrcid());
    assertNull(egoConfig.getApi().getSso().getLinkedin());
    assertNotNull(egoConfig.getApi().getSso().getGoogle());
    val google = egoConfig.getApi().getSso().getGoogle();
    assertEquals("googleClientId", google.getClientId());
    assertEquals("googleClientSecret", google.getClientSecret());
    assertEquals("some.redirect.google.example.org", google.getPreEstablishedRedirectUri());
    assertEquals("dms", egoConfig.getApi().getDmsAppCredential().getName());
    assertEquals("dms", egoConfig.getApi().getDmsAppCredential().getClientId());
    assertEquals(30, egoConfig.getApi().getDmsAppCredential().getClientSecret().length());
    assertEquals(9002, egoConfig.getUi().getHostPort());
    assertEquals(new URL("https://dms.example.org:9933/hi/there/ui"), egoConfig.getUi().getUrl());
  }

  @Test
  @SneakyThrows
  public void egoQuestionnaire_localMode_success() {
    val inputs =
        List.of(
            "30",
            "3",
            "5",
            "12",
            "9000",
            "1",
            "googleClientId",
            "googleClientSecret",
            "dms",
            "30",
            "N",
            "9001",
            "9002");

    this.testTextTerminal.getInputs().addAll(inputs);
    val egoConfig =
        handleCall(
            () -> this.egoQuestionnaire.buildEgoConfig(LOCAL),
            Throwable.class,
            e -> {
              log.error(e.getMessage());
              log.info("Terminal Output: " + testTextTerminal.getOutputAndReset(false));
            });

    assertEquals(30, egoConfig.getApi().getTokenDurationDays());
    assertEquals(
        Duration.of(3, HOURS).toMillis(), egoConfig.getApi().getJwt().getUser().getDurationMs());
    assertEquals(
        Duration.of(5, HOURS).toMillis(), egoConfig.getApi().getJwt().getApp().getDurationMs());
    assertEquals(Duration.of(12, HOURS).toMillis(), egoConfig.getApi().getRefreshTokenDurationMS());
    assertEquals(9000, egoConfig.getApi().getHostPort());
    assertEquals(new URL("http://localhost:9000"), egoConfig.getApi().getUrl());
    assertNull(egoConfig.getDb().getDatabasePassword());
    assertEquals(9001, egoConfig.getDb().getHostPort());
    assertNull(egoConfig.getApi().getSso().getGithub());
    assertNull(egoConfig.getApi().getSso().getFacebook());
    assertNull(egoConfig.getApi().getSso().getOrcid());
    assertNull(egoConfig.getApi().getSso().getLinkedin());
    assertNotNull(egoConfig.getApi().getSso().getGoogle());
    val google = egoConfig.getApi().getSso().getGoogle();
    assertEquals("googleClientId", google.getClientId());
    assertEquals("googleClientSecret", google.getClientSecret());
    assertEquals("http://localhost:9000/oauth/login/google", google.getPreEstablishedRedirectUri());
    assertEquals("dms", egoConfig.getApi().getDmsAppCredential().getName());
    assertEquals("dms", egoConfig.getApi().getDmsAppCredential().getClientId());
    assertEquals(30, egoConfig.getApi().getDmsAppCredential().getClientSecret().length());
    assertEquals(9002, egoConfig.getUi().getHostPort());
    assertEquals(new URL("http://localhost:9002"), egoConfig.getUi().getUrl());
  }
}
