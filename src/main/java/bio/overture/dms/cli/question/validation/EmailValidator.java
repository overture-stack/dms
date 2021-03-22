package bio.overture.dms.cli.question.validation;

import java.util.List;
import java.util.regex.Pattern;

public class EmailValidator implements QuestionValidator<String> {

  public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

  @Override
  public List<String> getErrorMessages(String val) {
    if (VALID_EMAIL_ADDRESS_REGEX.matcher(val).matches()) {
      return null;
    }
    return List.of("The provided value is not a valid email");
  }
}
