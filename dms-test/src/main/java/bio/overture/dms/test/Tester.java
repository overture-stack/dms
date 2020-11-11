package bio.overture.dms.test;

import bio.overture.dms.core.Nullable;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;

import static bio.overture.dms.core.Strings.isBlank;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

/**
 * Contains useful utilities for testing
 */
@NoArgsConstructor(access = PRIVATE)
public class Tester {

  public static void assertExceptionThrown(Runnable runnable, Class<? extends Exception> exceptionClass){
    assertExceptionThrown(runnable, exceptionClass, null);
  }

  public static void assertExceptionThrown(Runnable runnable, Class<? extends Exception> exceptionClass, @Nullable String containingTextInMessage){
    try{
      runnable.run();
    } catch (Exception e){
      assertTrue(exceptionClass.isInstance(e),
          "Expected exception of type: '%s' but got '%s'. Error was: %s",
          exceptionClass.getName(), e.getClass().getName(), e.getMessage() );
      if (!isBlank(containingTextInMessage)){
        assertTrue(e.getMessage().contains(containingTextInMessage),
            "Expected the following error message to contain the text '%s' but did not: %s",
            containingTextInMessage, e.getMessage());
      }
    }
  }

  public static void assertTrue(boolean exp, String formattedMessage, Object ...args){
    Assertions.assertTrue(exp, format(formattedMessage, args));
  }

  public static void assertFalse(boolean exp, String formattedMessage, Object ...args){
    Assertions.assertFalse(exp, format(formattedMessage, args));
  }

  public static void assertEquals(Object expected,  Object actual, String formattedMessage, Object ...args){
    Assertions.assertEquals(expected, actual, format(formattedMessage, args));
  }

}
