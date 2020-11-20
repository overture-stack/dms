package bio.overture.dms.infra.graph;

import bio.overture.dms.infra.model.Nameable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static bio.overture.dms.core.exception.NotFoundException.buildNotFoundException;
import static bio.overture.dms.core.exception.NotFoundException.checkNotFound;
import static java.util.stream.Collectors.toUnmodifiableSet;

@RequiredArgsConstructor
public abstract class AbstractGraph<T extends Nameable, N extends Node<T>> {

  @NonNull protected final Map<String, N> nameMap;
  @NonNull protected final Map<N, Set<N>> nodeMap;

  // TODO: test getting root nodes
  public Set<N> getRoots() {
    val childSet = nodeMap.values().stream()
        .flatMap(Collection::stream)
        .collect(toUnmodifiableSet());
    return nodeMap.keySet().stream()
        .filter(x -> !childSet.contains(x))
        .collect(toUnmodifiableSet());
  }

  public Set<N> getChildNodes(@NonNull T data){
    return getChildNodes(data.getName());
  }

  public Set<N> getChildNodes(@NonNull String parentName){
    return getChildNodes(getNode(parentName));
  }

  public Set<N> getChildNodes(@NonNull Node<T> parent) {
    checkNotFound(nodeMap.containsKey(parent),
        "Could not find node with name: %s", parent.getData().getName());
    return nodeMap.get(parent).stream()
        .collect(toUnmodifiableSet());
  }

  public N getNode(@NonNull T data) {
    return getNode(data.getName());
  }

  public N getNode(@NonNull String name) {
    return findNodeByName(name)
        .orElseThrow(() -> buildNotFoundException("Could not find node with name: %s", name));
  }

  public Optional<N> findNodeByName(@NonNull String name) {
    return Optional.ofNullable(nameMap.get(name));
  }

  public int numNodes() {
    return nameMap.size();
  }

  public static <T extends Nameable> GraphBuilder<T> builder(){
    return new GraphBuilder<>();
  }



}
