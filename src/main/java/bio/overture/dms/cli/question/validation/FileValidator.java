package bio.overture.dms.cli.question.validation;

import java.io.File;
import java.util.List;
import lombok.val;

public class FileValidator implements QuestionValidator<String> {

  @Override
  public List<String> getErrorMessages(String path) {
    try {
      val fileExists = new File(path).exists();
      if (fileExists) return null;
      return List.of("File not found");
    } catch (Exception e) {
      return List.of("Couldn't check the provided file");
    }
  }
}
