package bio.overture.dms.util;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public class CollectionUtils {

  public static <T> Set<T> intersection(@NonNull Collection<T> left, @NonNull Collection<T> right) {
    val leftSet = Set.copyOf(left);
    return right.stream().filter(leftSet::contains).collect(toUnmodifiableSet());
  }

  public static <T> List<T> newArrayList(@NonNull Collection<T> collection) {
    return new ArrayList<>(collection);
  }

  public static <I, O> Stream<O> mapToStream(
      @NonNull Collection<I> inputItems, @NonNull Function<I, O> function) {
    return inputItems.stream().map(function);
  }

  public static <I, O> List<O> mapToUnmodifiableList(
      @NonNull Collection<I> inputItems, @NonNull Function<I, O> function) {
    return mapToStream(inputItems, function).collect(toUnmodifiableList());
  }

  public static <I, O> List<O> mapToList(
      @NonNull Collection<I> inputItems, @NonNull Function<I, O> function) {
    return mapToStream(inputItems, function).collect(toList());
  }

  public static <I, O> Set<O> mapToUnmodifiableSet(
      @NonNull Collection<I> inputItems, @NonNull Function<I, O> function) {
    return mapToStream(inputItems, function).collect(toUnmodifiableSet());
  }

  public static <I, O> Set<O> mapToSet(
      @NonNull Collection<I> inputItems, @NonNull Function<I, O> function) {
    return mapToStream(inputItems, function).collect(toSet());
  }
}
