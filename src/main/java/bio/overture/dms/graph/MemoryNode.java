package bio.overture.dms.graph;

import bio.overture.dms.model.Nameable;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MemoryNode<T extends Nameable> extends Node<T> {

  /** Data */
  private final int maxUnvisitedParents;

  public MemoryNode(T data, int maxUnvisitedParents) {
    super(data, new AtomicInteger(maxUnvisitedParents));
    this.maxUnvisitedParents = maxUnvisitedParents;
  }

  public void reset() {
    this.getNumUnvisitedParents().set(maxUnvisitedParents);
  }

  public MemoryNode<T> copy() {
    return new MemoryNode<>(getData(), maxUnvisitedParents);
  }

  public static <T extends Nameable> MemoryNode<T> of(Node<T> node) {
    return new MemoryNode<>(node.getData(), node.getNumUnvisitedParents().get());
  }
}
