package bio.overture.dms.cli.model.question.impl;

import bio.overture.dms.cli.model.question.PasswordQuestion;
import bio.overture.dms.cli.model.question.RequiredQuestion;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PasswordQuestionDecorator implements PasswordQuestion {

  @NonNull private final RequiredQuestion<String> internalQuestion;

  @Override public String getQuestion() {
    return internalQuestion.getQuestion();
  }

  @Override public String getAnswer() {
    return internalQuestion.getAnswer();
  }

  @Override public void setAnswer(String s) {
    internalQuestion.setAnswer(s);
  }
}
