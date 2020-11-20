package bio.overture.dms.infra.job;

import bio.overture.dms.infra.model.Nameable;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import static bio.overture.dms.core.Exceptions.joinStackTrace;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Value
@Builder
public class DeployJob implements Nameable, Runnable {

  @NonNull private final String name;
  @NonNull @Getter(value = PRIVATE) private final Runnable deployTask;

  @Override
  public void run(){
    try{
      deployTask.run();
    } catch (Throwable t){
      log.error("ERROR: [{}] {}: {}", t.getClass().getName(), t.getMessage(), joinStackTrace(t));
      throw t;
    }

  }
}
