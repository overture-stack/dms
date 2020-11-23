package bio.overture.dms.cli.model.question;

public interface OptionalQuestion<T> extends RequiredQuestion<T> {

  T getDefaultAnswer();

}
