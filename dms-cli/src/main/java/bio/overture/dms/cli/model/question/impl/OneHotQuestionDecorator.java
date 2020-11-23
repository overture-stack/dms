package bio.overture.dms.cli.model.question.impl;

import bio.overture.dms.cli.model.question.OneHotQuestionFormat;
import bio.overture.dms.cli.model.question.RequiredQuestion;
import bio.overture.dms.core.util.Nullable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class OneHotQuestionDecorator<T> implements OneHotQuestionFormat<T> {

  @NonNull private final RequiredQuestion<T> internalQuestion;
  @NonNull private final Set<T> selections;

  @Override public String getQuestion() {
    return internalQuestion.getQuestion();
  }

  @Override public T getAnswer() {
    return internalQuestion.getAnswer();
  }

  @Override
  public void setAnswer(@Nullable T t) {
    internalQuestion.setAnswer(t);
  }
}
