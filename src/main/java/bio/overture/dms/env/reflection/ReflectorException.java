package bio.overture.dms.env.reflection;

import static java.lang.String.format;

import lombok.NonNull;

public class ReflectorException extends RuntimeException {

  public ReflectorException(@NonNull String formattedMessage, Object... args) {
    super(format(formattedMessage, args));
  }

  public ReflectorException(Throwable cause, @NonNull String formattedMessage, Object... args) {
    super(format(formattedMessage, args), cause);
  }

  public ReflectorException(
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace,
      @NonNull String formattedMessage,
      Object... args) {
    super(format(formattedMessage, args), cause, enableSuppression, writableStackTrace);
  }

  public static void checkReflection(boolean expression, String formattedMessage, Object... args) {
    if (!expression) {
      throw buildReflection(formattedMessage, args);
    }
  }

  public static ReflectorException buildReflection(
      @NonNull String formattedMessage, Object... args) {
    return new ReflectorException(formattedMessage, args);
  }
}
