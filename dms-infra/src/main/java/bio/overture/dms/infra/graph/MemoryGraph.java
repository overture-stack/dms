package bio.overture.dms.infra.graph;

import bio.overture.dms.core.CollectionUtils;
import bio.overture.dms.infra.model.Nameable;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static bio.overture.dms.core.CollectionUtils.mapToUnmodifiableSet;

public class MemoryGraph<T extends Nameable> extends AbstractGraph<T, MemoryNode<T>> {

  public MemoryGraph(@NonNull Map<String, MemoryNode<T>> nameMap,
      @NonNull Map<MemoryNode<T>, Set<MemoryNode<T>>> nodeMap) {
    super(nameMap, nodeMap);
  }

  public void reset(){
    nameMap.values().forEach(MemoryNode::reset);
  }

  public MemoryGraph<T> copy(){
    val newNameMap = new HashMap<String, MemoryNode<T>>();
    val newNodeMap = new HashMap<MemoryNode<T>, Set<MemoryNode<T>>>();
    for (val entry : nameMap.entrySet()){
      val nodeName = entry.getKey();
      val node = entry.getValue();
      val nodeCopy = node.copy();

      newNameMap.put(nodeName, nodeCopy);
      val copyChildren = mapToUnmodifiableSet(nodeMap.get(node), MemoryNode::copy);
      newNodeMap.put(nodeCopy, copyChildren);
    }
    return new MemoryGraph<>(Map.copyOf(newNameMap), Map.copyOf(newNodeMap));
  }

}
