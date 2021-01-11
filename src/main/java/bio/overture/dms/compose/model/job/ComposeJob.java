package bio.overture.dms.compose.model.job;

import static bio.overture.dms.core.util.Exceptions.joinStackTrace;
import static lombok.AccessLevel.PRIVATE;

import bio.overture.dms.core.model.Nameable;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@Builder
public class ComposeJob implements Nameable, Runnable {

  @NonNull private final String name;

  @NonNull
  @Getter(value = PRIVATE)
  private final Runnable deployTask;

  @Override
  public void run() {
    try {
      deployTask.run();
    } catch (Throwable t) {
      log.error("ERROR: [{}] {}: {}", t.getClass().getName(), t.getMessage(), joinStackTrace(t));
      throw t;
    }
  }
}
