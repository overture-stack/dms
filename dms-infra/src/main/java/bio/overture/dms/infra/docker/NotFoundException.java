package bio.overture.dms.infra.docker;

import bio.overture.dms.infra.converter.InvalidSpecException;
import lombok.NonNull;
import lombok.val;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.lang.String.format;

public class NotFoundException extends RuntimeException{
  public NotFoundException(@NonNull String formattedMessage, Object ... args) {
    super(format(formattedMessage, args));
  }

  public NotFoundException(Throwable cause, @NonNull String formattedMessage, Object ... args) {
    super(format(formattedMessage , args), cause);
  }

  public NotFoundException(Throwable cause, boolean enableSuppression,
      boolean writableStackTrace, @NonNull String formattedMessage, Object ...args ) {
    super(format(formattedMessage, args), cause, enableSuppression, writableStackTrace);
  }

  public static void checkNotFound(boolean expression, String formattedMessage, Object ...args){
    if (!expression){
      throw buildNotFoundException(formattedMessage, args);
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkNotFound(boolean expression, String formattedMessage, Supplier<Object> ... suppliers){
    val args = Arrays.stream(suppliers)
        .map(Supplier::get)
        .toArray();
    checkNotFound(expression, formattedMessage, args);
  }


  public static NotFoundException buildNotFoundException(@NonNull String formattedMessage, Object ... args){
    return new NotFoundException(formattedMessage, args);
  }

}
