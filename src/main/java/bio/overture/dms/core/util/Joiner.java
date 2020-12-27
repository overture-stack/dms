package bio.overture.dms.core.util;

import static java.io.File.separator;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PRIVATE)
public class Joiner {
  public static final Joiner NONE = Joiner.on("");
  public static final Joiner COMMA = Joiner.on(",");
  public static final Joiner WHITESPACE = Joiner.on(" ");
  public static final Joiner NEWLINE = Joiner.on(lineSeparator());
  public static final Joiner EQUALS = Joiner.on("=");
  public static final Joiner COLON = Joiner.on(":");
  public static final Joiner PATH = Joiner.on(separator);

  @NonNull private final String delimiter;

  public String join(Collection<?> collection) {
    return join(collection.stream());
  }

  public String join(Stream<?> stream) {
    return stream.map(Object::toString).collect(joining(delimiter));
  }

  public String join(Object... args) {
    return join(stream(args));
  }

  public static Joiner on(String delimiter) {
    return new Joiner(delimiter);
  }
}
