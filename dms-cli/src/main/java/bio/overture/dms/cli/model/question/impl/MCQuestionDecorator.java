package bio.overture.dms.cli.model.question.impl;

import bio.overture.dms.cli.model.question.MCQuestionFormat;
import bio.overture.dms.cli.model.question.RequiredQuestion;
import bio.overture.dms.core.util.Nullable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Set;
import java.util.TreeSet;

@Value
@Builder
public class MCQuestionDecorator<T> implements MCQuestionFormat<T> {

  @NonNull private final RequiredQuestion<Set<T>> internalQuestion;
  @NonNull private final Set<T> selections;

  @Override public String getQuestion() {
    return internalQuestion.getQuestion();
  }

  @Override public Set<T> getAnswer() {
    return internalQuestion.getAnswer();
  }

  @Override public void setAnswer(@Nullable Set<T> ts) {
    internalQuestion.setAnswer(ts);
  }
}
