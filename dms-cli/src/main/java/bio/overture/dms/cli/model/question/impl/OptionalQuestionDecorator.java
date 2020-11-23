package bio.overture.dms.cli.model.question.impl;

import bio.overture.dms.cli.model.question.OptionalQuestion;
import bio.overture.dms.cli.model.question.RequiredQuestion;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import static java.util.Objects.isNull;

@Value
@Builder
public class OptionalQuestionDecorator<T> implements OptionalQuestion<T> {

  @NonNull private final RequiredQuestion<T> internalQuestion;
  @NonNull private final T defaultAnswer;


  @Override
  public String getQuestion() {
    return internalQuestion.getQuestion();
  }

  @Override
  public T getAnswer() {
    return isNull(internalQuestion.getAnswer()) ? getDefaultAnswer() : internalQuestion.getAnswer();
  }

  @Override
  public void setAnswer(T t) {
    internalQuestion.setAnswer(t);
  }

}
