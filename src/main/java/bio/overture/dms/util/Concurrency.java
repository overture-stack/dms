package bio.overture.dms.util;

import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;
import static lombok.Lombok.sneakyThrow;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
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

  @SneakyThrows
  public static <T> T getFuture(@NonNull Future<T> f) {
    return f.get();
  }

  public static <T> List<T> getFutures(@NonNull Collection<? extends Future<T>> futures) {
    return futures.stream().map(Concurrency::getFuture).collect(toUnmodifiableList());
  }
}
