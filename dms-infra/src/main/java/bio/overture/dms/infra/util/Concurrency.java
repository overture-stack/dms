package bio.overture.dms.infra.util;

import bio.overture.dms.infra.job.DeployJobCallback;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.ExecutorService;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class Concurrency {

  public static void trySubmit(ExecutorService e, Runnable r){
    e.submit(() -> {
      try{
        r.run();
      }catch (Throwable t){
        val stackTrace = stream(t.getStackTrace())
            .map(StackTraceElement::toString)
            .collect(joining("\n"));
        log.error("ERROR: [{}] {}: {}", t.getClass().getName(), t.getMessage(), stackTrace);
        throw t;
      }
    });

  }
}
