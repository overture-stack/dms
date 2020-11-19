package bio.overture.dms.infra.util;

import bio.overture.dms.infra.job.DeployJobCallback;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class Concurrency {

  public static void trySubmit(ExecutorService e, Runnable r, Runnable onError){
    e.submit(() -> {
      try{
        r.run();
      }catch (Throwable t){
        log.error("ERROR: [{}] {}: {}", t.getClass().getName(), t.getMessage(), Exceptions.joinStackTrace(t));
        onError.run();
        throw t;
      }
    });

  }

  public static void trySubmit(ExecutorService e, Runnable r){
    trySubmit(e, r, () -> {});
  }

}
