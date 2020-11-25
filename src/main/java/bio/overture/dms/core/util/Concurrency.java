package bio.overture.dms.core.util;

import static lombok.AccessLevel.PRIVATE;
import static lombok.Lombok.sneakyThrow;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class Concurrency {

  public static void trySubmit(ExecutorService e, Runnable r, Runnable onError) {
    e.submit(
        () -> {
          try {
            r.run();
          } catch (Throwable t) {
            log.error(
                "ERROR: [{}] {}: {}",
                t.getClass().getName(),
                t.getMessage(),
                Exceptions.joinStackTrace(t));
            onError.run();
            throw t;
          }
        });
  }

  public static void trySubmit(ExecutorService e, Runnable r) {
    trySubmit(e, r, () -> {});
  }

  public static void waitForFutures(Collection<? extends Future<?>> futures) {
    futures.forEach(
        x -> {
          try {
            x.get();
          } catch (InterruptedException | ExecutionException e) {
            throw sneakyThrow(e);
          }
        });
  }
}
