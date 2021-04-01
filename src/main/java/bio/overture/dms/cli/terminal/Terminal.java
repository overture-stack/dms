package bio.overture.dms.cli.terminal;

import static bio.overture.dms.cli.model.enums.QuestionProfiles.ERROR;
import static bio.overture.dms.cli.model.enums.QuestionProfiles.LABEL;
import static bio.overture.dms.cli.model.enums.QuestionProfiles.LINK;
import static bio.overture.dms.cli.model.enums.QuestionProfiles.STATUS;
import static bio.overture.dms.cli.model.enums.QuestionProfiles.WARNING;
import static com.google.common.base.Strings.repeat;

import bio.overture.dms.cli.model.enums.QuestionProfiles;
import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.val;

public interface Terminal {

  int getTerminalWidth();

  Terminal printProfile(QuestionProfiles profile, String formattedText, Object... args);

  Terminal print(String formattedText, Object... args);

  default Terminal printLine() {
    println(line());
    return this;
  }

  Terminal resetLine();
  Terminal clear();

  default Terminal printStatus(@NonNull String formattedText, Object... args) {
    return clearLine().printProfile(STATUS, "\r" + formattedText, args);
  }

  default Terminal printStatusLn(@NonNull String formattedText, Object... args) {
    return printStatus(formattedText, args).println();
  }

  default Terminal printError(@NonNull String formattedText, Object... args) {
    return clearLine().printProfile(ERROR, formattedText, args);
  }

  default Terminal printErrorLn(@NonNull String formattedText, Object... args) {
    return printError(formattedText, args).println();
  }

  default Terminal printLink(@NonNull String formattedText, Object... args) {
    return clearLine().printProfile(LINK, formattedText, args);
  }

  default Terminal printWarning(@NonNull String text, Object... args) {
    return clearLine().printProfile(WARNING, text, args);
  }

  default Terminal printLabel(@NonNull String text, Object... args) {
    return clearLine().printProfile(LABEL, text, args);
  }

  default Terminal println() {
    return println("");
  }

  default Terminal println(@NonNull String text, Object... args) {
    return print(text + "\n", args);
  }

  default Terminal println(
      @NonNull QuestionProfiles profile, @NonNull String text, Object... args) {
    return printProfile(profile, text + "\n", args);
  }

  default String line() {
    return Strings.repeat("-", getTerminalWidth());
  }

  default Terminal clearLine() {
    val padding = repeat(" ", getTerminalWidth());
    return print("\r" + padding + "\r");
  }
}
