package bio.overture.dms.cli.question;

import lombok.Lombok;
import lombok.SneakyThrows;
import lombok.val;
import org.beryx.textio.TextIO;
import org.beryx.textio.mock.MockTextTerminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static lombok.Lombok.sneakyThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuestionTest {

  private final static URL TEST_URL;
  static {
    try {
      TEST_URL = new URL("http://somehost:9987/some/path?key1=val1&key2=val2");
    } catch (MalformedURLException e) {
      throw sneakyThrow(e);
    }
  }

  /** State */
  private MockTextTerminal mockTextTerminal;
  private QuestionFactory questionFactory;

  @BeforeEach
  public void beforeTest() {
    this.mockTextTerminal = new MockTextTerminal();
    this.questionFactory = new QuestionFactory(new TextIO(mockTextTerminal));
  }

  private void enqueueAnswer(String inputString){
    mockTextTerminal.getInputs().add(inputString);
  }

  @Test
  public void urlQuestion_normal_success(){
    runRequiredQuestionTest(List.of(TEST_URL.toString()),0 );
  }

  @Test
  public void urlQuestion_invalidFollowedByCorrect_success(){
    val missingProtocol = TEST_URL.getAuthority()+TEST_URL.getFile();
    val invalidProtocol =  "abcdeffg://"+missingProtocol;
    val correctProtocol = "https://"+missingProtocol;
    runRequiredQuestionTest(
        List.of(
            missingProtocol,
            invalidProtocol,
            correctProtocol,
            invalidProtocol)
        , 2);
  }


  @Test
  public void urlOptionalQuestion_emptyInput_success(){
    runOptionalQuestionTest("", TEST_URL);
  }

  @Test
  public void urlOptionalQuestion_whiteSpaceOnlyInput_success(){
    runOptionalQuestionTest("   ", TEST_URL);
  }

  @Test
  public void urlOptionalQuestion_nullInput_success(){
    runOptionalQuestionTest(null, TEST_URL);
  }


  @SneakyThrows
  private void runRequiredQuestionTest(List<String> answers, int successfulAnswerIndex){

    // Enqueue the answers in the correct order
    answers.forEach(this::enqueueAnswer);

    val expectedUrl = new URL(answers.get(successfulAnswerIndex));

    // Ask the question
    val actualUrl = questionFactory.newUrlSingleQuestion("Enter a url:", false, null)
        .getAnswer();


    // Ensure the expected and actual match
    assertEquals(expectedUrl, actualUrl);

    //Ensure that the successfulAnswerIndex-th answer read was the correct one
    assertEquals(successfulAnswerIndex+1, mockTextTerminal.getReadCalls());
  }

  @SneakyThrows
  private void runOptionalQuestionTest(String answer, URL defaultUrl ){

    // Enqueue the answers in the correct order
    enqueueAnswer(answer);

    val expectedUrl = defaultUrl;

    // Ask the question
    val actualUrl = questionFactory.newUrlSingleQuestion("Enter a url:", true, defaultUrl)
        .getAnswer();


    // Ensure the expected and actual match
    assertEquals(expectedUrl, actualUrl);

    //Ensure that the successfulAnswerIndex-th answer read was the correct one
    assertEquals(1, mockTextTerminal.getReadCalls());
  }


}
