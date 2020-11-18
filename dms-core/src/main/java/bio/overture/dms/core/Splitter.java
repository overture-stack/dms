package bio.overture.dms.core;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

@RequiredArgsConstructor
public class Splitter {

  public static final Splitter WHITESPACE = Splitter.on("\\s+");
  public static final Splitter EQUALS = Splitter.on("=");
  public static final Splitter COLON = Splitter.on(":") ;

  @NonNull private final String regex;

  public List<String> split(Object o, boolean immutable){
    val stream = splitStream(o);
    return immutable ? stream.collect(toUnmodifiableList()) : stream.collect(toList());
  }

  public Stream<String> splitStream(@NonNull Object o){
    return Arrays.stream(o.toString().split(regex));
  }

  public static Splitter on(String regex){
    return new Splitter(regex);
  }

}
