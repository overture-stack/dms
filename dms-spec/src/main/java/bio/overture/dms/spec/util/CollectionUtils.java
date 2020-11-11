package bio.overture.dms.spec.util;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.util.Collection;
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

}
