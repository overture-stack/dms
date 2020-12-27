package bio.overture.dms.cli.terminal;

import static bio.overture.dms.core.util.Strings.isNotDefined;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;
import org.beryx.textio.InputReader;
import org.beryx.textio.TextTerminal;

public class UrlInputReader extends InputReader<URL, UrlInputReader> {

  public UrlInputReader(Supplier<TextTerminal<?>> textTerminalSupplier) {
    super(textTerminalSupplier);
  }

  @Override
  protected ParseResult<URL> parse(String s) {
    if (isNotDefined(s)) {
      return new ParseResult<>(null, getErrorMessages(s));
    }
    try {
      return new ParseResult<>(new URL(s));
    } catch (MalformedURLException e) {
      return new ParseResult<>(null, getErrorMessages(e.getMessage()));
    }
  }
}
