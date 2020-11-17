package bio.overture.dms.infra.graph;

import bio.overture.dms.infra.docker.NotFoundException;
import bio.overture.dms.infra.model.Nameable;
import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class GraphBuilder<T extends Nameable> {

  private final Map<String, Node<T>> nameMap = new HashMap<>();
  private final Map<Node<T>, Set<Node<T>>> nodeMap = new HashMap<>();

  public GraphBuilder<T> addEdge(@NonNull Node<T> parent, @NonNull Node<T> child) {
    initNode(parent);
    initNode(child);

    nodeMap.get(parent).add(child);
    child.incrDeps();
    return this;
  }

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
