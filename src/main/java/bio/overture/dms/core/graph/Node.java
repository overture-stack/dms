package bio.overture.dms.core.graph;

import bio.overture.dms.core.model.Nameable;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Node<T extends Nameable> {

  /** Data */
  @NonNull private final T data;

  /**
   * Stateful data representing the number of unproccessed/unvisited parent nodes, relative to this
   * node.
   */
  @NonNull @Builder.Default @EqualsAndHashCode.Exclude
  private final AtomicInteger numUnvisitedParents = new AtomicInteger(0);

  public Integer decrementUnvisitedParents() {
    return numUnvisitedParents.decrementAndGet();
  }

  public Integer incrementUnvisitedParents() {
    return numUnvisitedParents.incrementAndGet();
  }

  public static <T extends Nameable> Node<T> of(T data) {
    return new Node<T>(data, new AtomicInteger(0));
  }
}
