package bio.overture.dms.cli.terminal;

import static java.lang.String.format;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.beryx.textio.TextTerminal;

@RequiredArgsConstructor
public class TerminalImpl implements Terminal {

  /** Configuration. */
  private final boolean ansi;

  private final boolean silent;
  @Getter private final int terminalWidth;
  @NonNull private final TextTerminal<?> textTerminal;

  @Override
  public Terminal printProfile(String printProfile, String formattedText, Object... args) {
    if (!silent) {
      if (ansi) {
        textTerminal.executeWithPropertiesPrefix(
            printProfile, t -> t.print(format(formattedText, args)));
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
}
