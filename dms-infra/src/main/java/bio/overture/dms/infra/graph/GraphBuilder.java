package bio.overture.dms.infra.graph;

import bio.overture.dms.infra.model.Nameable;
import lombok.NonNull;
import lombok.val;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class GraphBuilder<T extends Nameable> extends AbstractGraph<T, Node<T>> {

  public GraphBuilder(){
    super(new HashMap<>(), new HashMap<>());
  }

  public GraphBuilder<T> addEdge(@NonNull Node<T> parent, @NonNull Node<T> child) {
    initNode(parent);
    initNode(child);

    nodeMap.get(parent).add(child);
    child.incrementUnvisitedParents();
    return this;
  }

  // TODO: test immutability
  public MemoryGraph<T> build(){
    val memoryNodeMap = new HashMap<MemoryNode<T>, Set<MemoryNode<T>>>();

    final Map<String, MemoryNode<T>> memoryNameMap = nameMap.entrySet().stream()
        .collect(toUnmodifiableMap(Map.Entry::getKey, x -> MemoryNode.of(x.getValue())));

    for (val e : nodeMap.entrySet()){
      val parentNode = e.getKey();

      val parentMemoryNode = memoryNameMap.get(parentNode.getData().getName());
      val childMemoryNodes = e.getValue().stream()
          .map(Node::getData)
          .map(Nameable::getName)
          .map(memoryNameMap::get)
          .collect(toUnmodifiableSet());
      memoryNodeMap.put(parentMemoryNode, childMemoryNodes);
    }
    return new MemoryGraph<>(memoryNameMap, Map.copyOf(memoryNodeMap));
  }

  private void initNode(Node<T> node) {
    if (!nameMap.containsKey(node.getData().getName())) {
      val emptySet = new HashSet<Node<T>>();
      nameMap.put(node.getData().getName(), node);
      nodeMap.put(node, emptySet);
    }

  }


}
