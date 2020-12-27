package bio.overture.dms.cli.question.validation;

import java.util.List;
import org.beryx.textio.InputReader.ValueChecker;

/** Functional interface for checking question answer constraints */
@FunctionalInterface
public interface QuestionValidator<T> {

  /**
   * Returns the list of error messages due to constraint violations caused by <code>val</code>
   *
   * @param val the value for which constraint violations are checked
   * @return - the list of error messages or null if no error has been detected.
   */
  List<String> getErrorMessages(T val);

  default ValueChecker<T> createValueChecker() {
    return (val, itemName) -> getErrorMessages(val);
  }
}
