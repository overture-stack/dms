package bio.overture.dms.infra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Exceptions {

  public static String joinStackTrace(Throwable t){
    return stream(t.getStackTrace())
        .map(StackTraceElement::toString)
        .collect(joining("\n"));
  }

}
