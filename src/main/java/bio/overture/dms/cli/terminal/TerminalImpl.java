package bio.overture.dms.cli.terminal;

import static java.lang.String.format;

import bio.overture.dms.cli.model.enums.QuestionProfiles;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.beryx.textio.TextTerminal;

@Builder
@RequiredArgsConstructor
public class TerminalImpl implements Terminal {

  /** Configuration. */
  private final boolean ansi;

  private final boolean silent;
  @Getter private final int terminalWidth;
  @NonNull private final TextTerminal<?> textTerminal;

  @Override
  public Terminal printProfile(QuestionProfiles profile, String formattedText, Object... args) {
    if (!silent) {
      if (ansi) {
        textTerminal.executeWithPropertiesPrefix(
            profile.toString(), t -> t.print(format(formattedText, args)));
      } else {
        return print(formattedText, args);
      }
    }
    return this;
  }

  @Override
  public TerminalImpl print(@NonNull String formattedText, Object... args) {
    if (!silent) {
      textTerminal.print(format(formattedText, args));
    }
    return this;
  }

  @Override
  public Terminal resetLine() {
    if (!silent) {
      textTerminal.resetLine();
    }
    return this;
  }

  public Terminal clear() {
    final String ANSI_CLS = "\033[2J";
    final String ANSI_HOME = "\033[H";
    textTerminal.print(ANSI_CLS + ANSI_HOME);
    return this;
  }

}
