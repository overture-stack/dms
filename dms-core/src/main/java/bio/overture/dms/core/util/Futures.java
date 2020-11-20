package bio.overture.dms.core.util;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Futures {

  @SneakyThrows
  public static <T> T getFuture(@NonNull Future<T> f){
    return f.get();
  }

  public static <T> List<T> getFutures(@NonNull Collection<? extends Future<T>> futures ){
    return futures.stream().map(Futures::getFuture).collect(toUnmodifiableList());
  }

  public static void waitForFutures(@NonNull Collection<? extends Future<?>> futures){
    futures.forEach(Futures::getFuture);
  }

}
