package bio.overture.dms.cli.model.question;

public interface SimpleQuestionFormat<T> extends RequiredQuestion<T> {
  boolean isPassword();
}
