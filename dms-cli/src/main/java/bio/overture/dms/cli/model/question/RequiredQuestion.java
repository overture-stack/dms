package bio.overture.dms.cli.model.question;

import java.util.Optional;

public interface RequiredQuestion<T> {

  String getQuestion();
  T getAnswer();
  void setAnswer(T t);

}
