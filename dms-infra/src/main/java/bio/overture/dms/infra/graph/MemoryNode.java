package bio.overture.dms.infra.graph;

import bio.overture.dms.infra.model.Nameable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MemoryNode<T extends Nameable> extends Node<T> {

  /**
   * Data
   */
  private final int maxUnvisitedParents;

  public MemoryNode(T data, int maxUnvisitedParents){
    super(data, new AtomicInteger(maxUnvisitedParents));
    this.maxUnvisitedParents = maxUnvisitedParents;
  }

  public void reset(){
    this.getNumUnvisitedParents().set(maxUnvisitedParents);
  }

  public static <T extends Nameable> MemoryNode<T> of(Node<T> node){
    return new MemoryNode<T>(node.getData(), node.getNumUnvisitedParents().get());
  }

}
