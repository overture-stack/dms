package bio.overture.dms.infra.job;

import bio.overture.dms.infra.model.Nameable;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@Builder
public class DeployJob implements Nameable {

  @NonNull private final String name;
  @NonNull private final Runnable deployTask;

  @SneakyThrows
  public void start(JobCallback callback) {
    deployTask.run();
    callback.onDone(this);
  }
}
