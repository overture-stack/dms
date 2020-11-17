package bio.overture.dms.core;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class Joiner {
  public static final Joiner COMMA = Joiner.on(",");
  public static final Joiner WHITESPACE = Joiner.on(" ");

  @NonNull private final String delimiter;

  public String join(Collection<?> collection){
    return join(collection.stream());
  }

  public String join(Stream<?> stream){
    return stream
        .map(Object::toString)
        .collect(joining(delimiter));
  }

  public static Joiner on(String delimiter){
    return new Joiner(delimiter);
  }

}
