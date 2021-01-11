package bio.overture.dms.graph;

import static bio.overture.dms.core.exception.NotFoundException.buildNotFoundException;
import static bio.overture.dms.core.exception.NotFoundException.checkNotFound;
import static java.util.stream.Collectors.toUnmodifiableSet;

import bio.overture.dms.core.model.Nameable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public abstract class AbstractGraph<T extends Nameable, N extends Node<T>> {

  @NonNull protected final Map<String, N> nodeMap;
  @NonNull protected final Map<N, Set<N>> connectionMap;

  // TODO: test getting root nodes
  public Set<N> getRoots() {
    val childSet =
        connectionMap.values().stream().flatMap(Collection::stream).collect(toUnmodifiableSet());
    return connectionMap.keySet().stream()
        .filter(x -> !childSet.contains(x))
        .collect(toUnmodifiableSet());
  }

  public Set<N> getChildNodes(@NonNull T data) {
    return getChildNodes(data.getName());
  }

  public Set<N> getChildNodes(@NonNull String parentName) {
    return getChildNodes(getNode(parentName));
  }

  public Set<N> getChildNodes(@NonNull Node<T> parent) {
    checkNotFound(
        connectionMap.containsKey(parent),
        "Could not find node with name: %s",
        parent.getData().getName());
    return connectionMap.get(parent).stream().collect(toUnmodifiableSet());
  }

  public N getNode(@NonNull T data) {
    return getNode(data.getName());
  }

  public N getNode(@NonNull String name) {
    return findNodeByName(name)
        .orElseThrow(() -> buildNotFoundException("Could not find node with name: %s", name));
  }

  public Optional<N> findNodeByName(@NonNull String name) {
    return Optional.ofNullable(nodeMap.get(name));
  }

  public int numNodes() {
    return nodeMap.size();
  }

  public static <T extends Nameable> GraphBuilder<T> builder() {
    return new GraphBuilder<>();
  }
}
