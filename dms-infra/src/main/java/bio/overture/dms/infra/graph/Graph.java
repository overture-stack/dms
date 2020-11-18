package bio.overture.dms.infra.graph;

import bio.overture.dms.infra.model.Nameable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static bio.overture.dms.infra.docker.NotFoundException.checkNotFound;
import static java.util.stream.Collectors.toUnmodifiableSet;

@RequiredArgsConstructor
public class Graph<T extends Nameable> {

  @NonNull private final Map<String, Node<T>> nameMap;
  @NonNull private final Map<Node<T>, Set<Node<T>>> nodeMap;

  // TODO: test getting root nodes
  public Set<Node<T>> getRoots() {
    val childSet = nodeMap.values().stream()
        .flatMap(Collection::stream)
        .collect(toUnmodifiableSet());
    return nodeMap.keySet().stream()
        .filter(x -> !childSet.contains(x))
        .collect(toUnmodifiableSet());
  }

  public Set<Node<T>> getChildNodes(@NonNull Node<T> parent) {
    checkNotFound(nodeMap.containsKey(parent),
        "Could not find node with name: {}", parent.getData().getName());
    return nodeMap.get(parent).stream()
        .collect(toUnmodifiableSet());
  }

  public Optional<Node<T>> getNodeByName(@NonNull String name) {
    return Optional.ofNullable(nameMap.get(name));
  }

  public int numNodes() {
    return nameMap.size();
  }

  public static <T extends Nameable> GraphBuilder<T> builder(){
    return new GraphBuilder<>();
  }

}
