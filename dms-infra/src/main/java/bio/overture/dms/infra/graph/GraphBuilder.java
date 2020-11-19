package bio.overture.dms.infra.graph;

import bio.overture.dms.infra.model.Nameable;
import lombok.NonNull;
import lombok.val;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableMap;

public class GraphBuilder<T extends Nameable> extends Graph<T> {

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
  public Graph<T> build(){
    final Map<Node<T>, Set<Node<T>>> immutableMap = nodeMap.entrySet().stream()
        .collect(toUnmodifiableMap(Map.Entry::getKey,
            y -> Set.copyOf(y.getValue())));
    return new Graph<>(Map.copyOf(nameMap), Map.copyOf(immutableMap));
  }

  private void initNode(Node<T> node) {
    if (!nameMap.containsKey(node.getData().getName())) {
      val emptySet = new HashSet<Node<T>>();
      nameMap.put(node.getData().getName(), node);
      nodeMap.put(node, emptySet);
    }

  }


}
