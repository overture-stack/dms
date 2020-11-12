package bio.overture.dms.core;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class CollectionUtils {

  public static <T> Set<T> intersection(@NonNull Collection<T> left, @NonNull Collection<T> right){
    val leftSet = Set.copyOf(left);
    return right.stream()
        .filter(leftSet::contains)
        .collect(toUnmodifiableSet());
  }

  public static <T> List<T> newArrayList(@NonNull Collection<T> collection){
    return new ArrayList<>(collection);
  }

}
