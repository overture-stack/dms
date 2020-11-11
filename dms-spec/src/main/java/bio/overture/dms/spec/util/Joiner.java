package bio.overture.dms.spec.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class Joiner {
  public static final Joiner COMMA = Joiner.on(",");

  @NonNull private final String delimiter;

  public String join(Collection<?> collection){
    return collection.stream()
        .map(Object::toString)
        .collect(joining(delimiter));
  }

  public static Joiner on(String delimiter){
    return new Joiner(delimiter);
  }

}
