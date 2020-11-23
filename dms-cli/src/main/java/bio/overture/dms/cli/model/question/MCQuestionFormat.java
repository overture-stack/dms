package bio.overture.dms.cli.model.question;

import java.util.Set;

public interface MCQuestionFormat<T> extends RequiredQuestion<Set<T>> {

  Set<T> getSelections();

}
