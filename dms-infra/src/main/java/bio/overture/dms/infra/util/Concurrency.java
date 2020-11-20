package bio.overture.dms.infra.util;

import bio.overture.dms.core.Exceptions;
import lombok.Lombok;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static lombok.Lombok.sneakyThrow;

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

  public static void waitForFutures(Collection<? extends Future<?>> futures){
    futures.forEach(x -> {
      try {
        x.get();
      } catch (InterruptedException | ExecutionException e) {
        throw sneakyThrow(e);
      }
    });

  }

}
