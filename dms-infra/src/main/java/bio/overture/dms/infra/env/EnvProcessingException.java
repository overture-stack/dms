package bio.overture.dms.infra.env;

import lombok.NonNull;

import static java.lang.String.format;

public class EnvProcessingException extends RuntimeException {

  public EnvProcessingException(@NonNull String formattedMessage, Object ... args) {
    super(format(formattedMessage, args));
  }

  public EnvProcessingException(Throwable cause, @NonNull String formattedMessage, Object ... args) {
    super(format(formattedMessage , args), cause);
  }

  public EnvProcessingException(Throwable cause, boolean enableSuppression,
      boolean writableStackTrace, @NonNull String formattedMessage, Object ...args ) {
    super(format(formattedMessage, args), cause, enableSuppression, writableStackTrace);
  }

  public static void checkEnvProcessing(boolean expression, String formattedMessage, Object ...args){
    if (!expression){
      throw buildEnvProcessingException(formattedMessage, args);
    }
  }

  public static EnvProcessingException buildEnvProcessingException(@NonNull String formattedMessage, Object ... args){
    return new EnvProcessingException(formattedMessage, args);
  }

}
