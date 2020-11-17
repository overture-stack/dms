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

import static java.util.stream.Collectors.toUnmodifiableSet;

public class Graph<T extends Nameable> {

  private final Map<String, Node<T>> nameMap = new HashMap<>();
  private final Map<Node<T>, Set<Node<T>>> nodeMap = new HashMap<>();

  public int numNodes() {
    return nameMap.size();
  }

  private void initNode(Node<T> node) {
    if (!nameMap.containsKey(node.getData().getName())) {
      val emptySet = new HashSet<Node<T>>();
      nameMap.put(node.getData().getName(), node);
      nodeMap.put(node, emptySet);
    }

  }

  public void addEdge(@NonNull Node<T> parent, @NonNull Node<T> child) {
    initNode(parent);
    initNode(child);

    nodeMap.get(parent).add(child);
    child.incrDeps();
  }

  public Set<Node<T>> getRoots() {
    val childSet = nodeMap.values().stream()
        .flatMap(Collection::stream)
        .collect(toUnmodifiableSet());
    return nodeMap.keySet().stream()
        .filter(x -> !childSet.contains(x))
        .collect(toUnmodifiableSet());
  }

  public Set<Node<T>> getChildNodes(@NonNull Node<T> parent) {
    NotFoundException.checkNotFound(nodeMap.containsKey(parent),
        "Could not find node with name: {}", parent.getData().getName());
    return nodeMap.get(parent).stream()
        .collect(toUnmodifiableSet());
  }

  public Optional<Node<T>> getNodeByName(@NonNull String name) {
    return Optional.ofNullable(nameMap.get(name));
  }

}
