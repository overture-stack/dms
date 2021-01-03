package bio.overture.dms.core;

import lombok.NonNull;

import static java.lang.String.format;

@FunctionalInterface
public interface Messenger {

  void send(String message);

  default void send(@NonNull String formattedMessage, Object... args) {
    send(format(formattedMessage, args));
  }

  default void send(@NonNull Object sourceObject, @NonNull String formattedMessage, Object... args) {
    send("[" + sourceObject.getClass().getSimpleName() + "]: " + formattedMessage, args);
  }

}
