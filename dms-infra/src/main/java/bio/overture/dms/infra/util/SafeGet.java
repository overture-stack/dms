package bio.overture.dms.infra.util;

import bio.overture.dms.core.Nullable;
import bio.overture.dms.infra.docker.DCServiceStateReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class SafeGet<T, R> {

  @Nullable private final T t;
  @NonNull private final Function<T, R> transformer;

  public <Z> SafeGet<R, Z> map(Function<R, Z> newTransformer) {
    if (!isNull(t)) {
      val r = transformer.apply(t);
      return new SafeGet<>(r, newTransformer);
    } else {
      return new SafeGet<>(null, newTransformer);
    }
  }

  public void ifPresent(Consumer<R> consumer) {
    if (!isNull(t)) {
      consumer.accept(transformer.apply(t));
    }
  }

  public Optional<R> get() {
    if (!isNull(t)) {
      return Optional.ofNullable(transformer.apply(t));
    }
    return Optional.empty();
  }

  public static <T, R> SafeGet<T, R> of(T t, Function<T, R> func) {
    return new SafeGet<>(t, func);
  }

}
