package bio.overture.dms.infra.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.HOURS;

@Slf4j
@RequiredArgsConstructor
public class SpringExecutorService implements ExecutorService {

  @NonNull private final ExecutorService delgate;
  private final int timeoutHours;

  @PreDestroy
  @SneakyThrows
  public void destroy() {
    log.info("Shutting down Spring executor service with timeoutHours={} ...", timeoutHours);
    shutdown();
    awaitTermination(timeoutHours, HOURS);
    log.info("Successfully shutdown Spring executor service");
  }

  /**
   * Delegated methods
   */
  @Override
  public void shutdown() {
    delgate.shutdown();
  }

  @Override public List<Runnable> shutdownNow() {
    return delgate.shutdownNow();
  }

  @Override public boolean isShutdown() {
    return delgate.isShutdown();
  }

  @Override public boolean isTerminated() {
    return delgate.isTerminated();
  }

  @Override public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return delgate.awaitTermination(timeout, unit);
  }

  @Override public <T> Future<T> submit(Callable<T> task) {
    return delgate.submit(task);
  }

  @Override public <T> Future<T> submit(Runnable task, T result) {
    return delgate.submit(task, result);
  }

  @Override public Future<?> submit(Runnable task) {
    return delgate.submit(task);
  }

  @Override public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    return delgate.invokeAll(tasks);
  }

  @Override public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
      TimeUnit unit) throws InterruptedException {
    return delgate.invokeAll(tasks, timeout, unit);
  }

  @Override public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    return delgate.invokeAny(tasks);
  }

  @Override public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return delgate.invokeAny(tasks, timeout, unit);
  }

  @Override public void execute(Runnable command) {
    delgate.execute(command);
  }
}
