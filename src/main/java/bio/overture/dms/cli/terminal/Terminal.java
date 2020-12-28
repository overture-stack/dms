package bio.overture.dms.cli.terminal;

import static com.google.common.base.Strings.repeat;

import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.val;

public interface Terminal {

  int getTerminalWidth();

  Terminal printProfile(String printProfile, String formattedText, Object... args);

  Terminal print(String formattedText, Object... args);

  default Terminal printLine() {
    println(line());
    return this;
  }

  default Terminal printStatus(@NonNull String formattedText, Object... args) {
    return clearLine().printProfile("status", "\r" + formattedText, args);
  }

  default Terminal printStatusLn(@NonNull String formattedText, Object... args) {
    return printStatus(formattedText, args).println();
  }

  default Terminal printError(@NonNull String formattedText, Object... args) {
    return clearLine().printProfile("error", formattedText, args);
  }

  default Terminal printErrorLn(@NonNull String formattedText, Object... args) {
    return printError(formattedText, args).println();
  }

  default Terminal printLink(@NonNull String formattedText, Object... args) {
    return clearLine().printProfile("link", formattedText, args);
  }

  default Terminal printWarning(@NonNull String text, Object... args) {
    return clearLine().printProfile("warning", text, args);
  }

  default Terminal printLabel(@NonNull String text, Object... args) {
    return clearLine().printProfile("label", text, args);
  }

  default Terminal println() {
    return println("");
  }

  default Terminal println(@NonNull String text, Object... args) {
    return print(text + "\n", args);
  }

  default Terminal println(@NonNull String prefix, @NonNull String text, Object... args) {
    return printProfile(prefix, text + "\n", args);
  }

  default String line() {
    return Strings.repeat("-", getTerminalWidth());
  }

  default Terminal clearLine() {
    val padding = repeat(" ", getTerminalWidth());
    return print("\r" + padding + "\r");
  }
}
