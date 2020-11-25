package bio.overture.dms.core.util;

import static java.util.Objects.isNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Safely calls argument-less methods while preventing Null Pointer Exceptions. It follows a similar
 * fluent style as the Optional class, however the {@param transformer} function callback is only
 * called when the input is NOT NULL. The Optional class does not check for null pointers between
 * subsequent Optional.map calls.
 */
@RequiredArgsConstructor
public class SafeGet<T, R> {

  @Nullable private final T value;
  @NonNull private final Function<T, R> transformer;

  /**
   * If the value contained in this instance is NOT NULL, the transformer will be called and a new
   * SafeGet instance will be returned for further processing
   *
   * @param newTransformer converts the contained {@link SafeGet#value} to a different the output
   *     type
   * @param <Z> The output type
   * @return a new instance of SafeGet with the transformed value
   */
  public <Z> SafeGet<R, Z> map(Function<R, Z> newTransformer) {
    if (!isNull(value)) {
      val r = transformer.apply(value);
      return new SafeGet<>(r, newTransformer);
    } else {
      return new SafeGet<>(null, newTransformer);
    }
  }

  public void ifPresent(Consumer<R> consumer) {
    if (!isNull(value)) {
      consumer.accept(transformer.apply(value));
    }
  }

  /** @return an optional representing the contained value */
  public Optional<R> get() {
    if (!isNull(value)) {
      return Optional.ofNullable(transformer.apply(value));
    }
    return Optional.empty();
  }

  public static <T, R> SafeGet<T, R> of(T t, Function<T, R> func) {
    return new SafeGet<>(t, func);
  }
}
