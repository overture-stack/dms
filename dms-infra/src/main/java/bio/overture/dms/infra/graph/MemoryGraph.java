package bio.overture.dms.infra.graph;

import bio.overture.dms.infra.model.Nameable;
import lombok.NonNull;

import java.util.Map;
import java.util.Set;

public class MemoryGraph<T extends Nameable> extends AbstractGraph<T, MemoryNode<T>> {

  public MemoryGraph(@NonNull Map<String, MemoryNode<T>> nameMap,
      @NonNull Map<MemoryNode<T>, Set<MemoryNode<T>>> nodeMap) {
    super(nameMap, nodeMap);
  }

  public void reset(){
    nameMap.values().forEach(MemoryNode::reset);
  }

}
