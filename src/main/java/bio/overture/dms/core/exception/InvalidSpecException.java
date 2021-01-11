package bio.overture.dms.core.exception;

import static java.lang.String.format;

import lombok.NonNull;

public class InvalidSpecException extends RuntimeException {

  public InvalidSpecException(@NonNull String formattedMessage, Object... args) {
    super(format(formattedMessage, args));
  }

  public InvalidSpecException(Throwable cause, @NonNull String formattedMessage, Object... args) {
    super(format(formattedMessage, args), cause);
  }

  public InvalidSpecException(
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace,
      @NonNull String formattedMessage,
      Object... args) {
    super(format(formattedMessage, args), cause, enableSuppression, writableStackTrace);
  }

  public static void checkInvalidSpec(boolean expression, String formattedMessage, Object... args) {
    if (!expression) {
      throw buildInvalidSpecException(formattedMessage, args);
    }
  }

  public static InvalidSpecException buildInvalidSpecException(
      @NonNull String formattedMessage, Object... args) {
    return new InvalidSpecException(formattedMessage, args);
  }
}
