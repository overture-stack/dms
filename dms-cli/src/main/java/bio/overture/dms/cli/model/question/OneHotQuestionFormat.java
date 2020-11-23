package bio.overture.dms.cli.model.question;

import java.util.Set;

public interface OneHotQuestionFormat<T> extends RequiredQuestion<T> {
  Set<T> getSelections();
}
