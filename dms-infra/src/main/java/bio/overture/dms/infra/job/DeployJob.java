package bio.overture.dms.infra.job;

import bio.overture.dms.infra.model.Nameable;
import bio.overture.dms.infra.util.Exceptions;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import static bio.overture.dms.infra.util.Exceptions.joinStackTrace;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Value
@Builder
public class DeployJob implements Nameable {

  @NonNull private final String name;
  @NonNull @Getter(value = PRIVATE) private final Runnable deployTask;

  @SneakyThrows
  public void start(JobCallback callback) {
    try{
      deployTask.run();
      callback.onDone(this);
    } catch (Throwable t){
      log.error("ERROR: [{}] {}: {}", t.getClass().getName(), t.getMessage(), joinStackTrace(t));
      callback.onError(this);
      throw t;
    }
  }
}
