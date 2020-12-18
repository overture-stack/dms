package bio.overture.dms.graph;

import static bio.overture.dms.core.util.CollectionUtils.mapToUnmodifiableSet;

import bio.overture.dms.core.model.Nameable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import lombok.val;

public class MemoryGraph<T extends Nameable> extends AbstractGraph<T, MemoryNode<T>> {

  public MemoryGraph(
      @NonNull Map<String, MemoryNode<T>> nodeMap,
      @NonNull Map<MemoryNode<T>, Set<MemoryNode<T>>> connectionMap) {
    super(nodeMap, connectionMap);
  }

  public void reset() {
    nodeMap.values().forEach(MemoryNode::reset);
  }

  public MemoryGraph<T> copy() {
    val newNodeMap = new HashMap<String, MemoryNode<T>>();
    val newConnectionMap = new HashMap<MemoryNode<T>, Set<MemoryNode<T>>>();
    for (val entry : nodeMap.entrySet()) {
      val nodeName = entry.getKey();
      val node = entry.getValue();
      val nodeCopy = node.copy();

      newNodeMap.put(nodeName, nodeCopy);
      val copyChildren = mapToUnmodifiableSet(connectionMap.get(node), MemoryNode::copy);
      newConnectionMap.put(nodeCopy, copyChildren);
    }
    return new MemoryGraph<>(Map.copyOf(newNodeMap), Map.copyOf(newConnectionMap));
  }
}
