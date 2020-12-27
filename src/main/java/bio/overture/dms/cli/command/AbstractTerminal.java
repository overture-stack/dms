package bio.overture.dms.cli.command;

import bio.overture.dms.cli.terminal.Terminal;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractTerminal implements Terminal {

  private final Terminal internalTerminal;

  @Override
  public int getTerminalWidth() {
    return internalTerminal.getTerminalWidth();
  }

  @Override
  public Terminal printProfile(String printProfile, String formattedText, Object... args) {
    return internalTerminal.printProfile(printProfile, formattedText, args);
  }

  @Override
  public Terminal print(String formattedText, Object... args) {
    return internalTerminal.print(formattedText, args);
  }

  @Override
  public Terminal printLine() {
    return internalTerminal.printLine();
  }

  @Override
  public Terminal printStatus(@NonNull String formattedText, Object... args) {
    return internalTerminal.printStatus(formattedText, args);
  }

  @Override
  public Terminal printStatusLn(@NonNull String formattedText, Object... args) {
    return internalTerminal.printStatusLn(formattedText, args);
  }

  @Override
  public Terminal printError(@NonNull String formattedText, Object... args) {
    return internalTerminal.printError(formattedText, args);
  }

  @Override
  public Terminal printLink(@NonNull String formattedText, Object... args) {
    return internalTerminal.printLink(formattedText, args);
  }

  @Override
  public Terminal printWarning(@NonNull String text, Object... args) {
    return internalTerminal.printWarning(text, args);
  }

  @Override
  public Terminal printLabel(@NonNull String text, Object... args) {
    return internalTerminal.printLabel(text, args);
  }

  @Override
  public Terminal println() {
    return internalTerminal.println();
  }

  @Override
  public Terminal println(@NonNull String text, Object... args) {
    return internalTerminal.println(text, args);
  }

  @Override
  public Terminal println(@NonNull String prefix, @NonNull String text, Object... args) {
    return internalTerminal.println(prefix, text, args);
  }

  @Override
  public String line() {
    return internalTerminal.line();
  }

  @Override
  public Terminal clearLine() {
    return internalTerminal.clearLine();
  }
}
