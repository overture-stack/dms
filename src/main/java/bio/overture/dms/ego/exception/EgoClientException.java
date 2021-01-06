package bio.overture.dms.ego.exception;

import static java.lang.String.format;

import lombok.NonNull;

public class EgoClientException extends RuntimeException {

  public EgoClientException(String message) {
    super(message);
  }

  public EgoClientException(String message, Throwable cause) {
    super(message, cause);
  }

  public static void checkEgoClient(boolean expression, String formattedMessage, Object... args) {
    if (!expression) {
      throw buildEgoClientException(formattedMessage, args);
    }
  }

  public static EgoClientException buildEgoClientException(
      @NonNull String formattedMessage, Object... args) {
    return new EgoClientException(format(formattedMessage, args));
  }
}
