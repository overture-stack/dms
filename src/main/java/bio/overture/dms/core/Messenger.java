package bio.overture.dms.core;

import static java.lang.String.format;

import lombok.NonNull;

@FunctionalInterface
public interface Messenger {

  void send(String message);

  default void send(@NonNull String formattedMessage, Object... args) {
    send(format(formattedMessage, args));
  }

  default void sendDetailed(
      @NonNull Object sourceObject, @NonNull String formattedMessage, Object... args) {
    send("[" + sourceObject.getClass().getSimpleName() + "]: " + formattedMessage, args);
  }
}
