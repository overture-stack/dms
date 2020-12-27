package bio.overture.dms.cli.question.validation;

import static bio.overture.dms.core.util.Strings.isNotDefined;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.val;

public class UrlQuestionValidator implements QuestionValidator<URL> {

  @Override
  public List<String> getErrorMessages(URL url) {
    if (isNull(url)) {
      return List.of("input url cannot be null");
    }
    val errors = new ArrayList<String>();

    if (isNotDefined(url.getProtocol())) {
      errors.add("The url protocol was not defined. Must be one of ['http', 'https']");
    } else if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
      errors.add(
          format("The url protocol '%s' is not one of ['http', 'https']", url.getProtocol()));
    }
    return null;
  }
}
