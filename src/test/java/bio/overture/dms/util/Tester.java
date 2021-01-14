package bio.overture.dms.util;

import static bio.overture.dms.core.util.Strings.isNotDefined;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import bio.overture.dms.core.util.Nullable;
import lombok.*;
import org.junit.jupiter.api.Assertions;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Contains useful utilities for testing */
@NoArgsConstructor(access = PRIVATE)
public class Tester {

  public static void assertExceptionThrown(
      Runnable runnable, Class<? extends Exception> exceptionClass) {
    assertExceptionThrown(runnable, exceptionClass, null);
  }

  /**
   * Try to execute the supplier and return the result.
   */
  @SuppressWarnings("unchecked")
  public static <T, E extends Throwable> T handleCall(@NonNull Supplier<T> supplier,
      @NonNull Class<E> errorClass,
      @NonNull Consumer<E> onExceptionFunction){
    try{
      return supplier.get();
    } catch (Throwable e){
      if (errorClass.isInstance(e)){
        val exception = (E)e;
        onExceptionFunction.accept(exception);
      }
      throw e;
    }
  }

  public static void assertExceptionThrown(
      Runnable runnable,
      Class<? extends Exception> exceptionClass,
      @Nullable String containingTextInMessage) {
    try {
      runnable.run();
    } catch (Exception e) {
      assertTrue(
          // Because of sneakythrows, need to get the cause aswell
          exceptionClass.isInstance(e) || exceptionClass.isInstance(e.getCause()),
          "Expected exception of type: '%s' but got '%s'. Error was: %s",
          exceptionClass.getName(),
          exceptionClass.isInstance(e) ? e.getClass().getName() : e.getCause().getClass().getName(),
          e.getMessage());
      if (!isNotDefined(containingTextInMessage)) {
        assertTrue(
            e.getMessage().contains(containingTextInMessage),
            "Expected the following error message to contain the text '%s' but did not: %s",
            containingTextInMessage,
            e.getMessage());
      }
    }
  }

  public static void assertTrue(boolean exp, String formattedMessage, Object... args) {
    Assertions.assertTrue(exp, format(formattedMessage, args));
  }

  public static void assertFalse(boolean exp, String formattedMessage, Object... args) {
    Assertions.assertFalse(exp, format(formattedMessage, args));
  }

  public static void assertEquals(
      Object expected, Object actual, String formattedMessage, Object... args) {
    Assertions.assertEquals(expected, actual, format(formattedMessage, args));
  }
}
