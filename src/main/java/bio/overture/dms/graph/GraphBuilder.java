package bio.overture.dms.graph;

import static bio.overture.dms.util.Exceptions.checkArgument;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

import bio.overture.dms.model.Nameable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import lombok.val;

public class GraphBuilder<T extends Nameable> extends AbstractGraph<T, Node<T>> {

  public GraphBuilder() {
    super(new HashMap<>(), new HashMap<>());
  }

  public GraphBuilder<T> addEdge(@NonNull String parentName, @NonNull String childName) {
    val parentNode = getNode(parentName);
    val childNode = getNode(childName);
    internalAddEdge(parentNode, childNode);
    return this;
  }

  public GraphBuilder<T> addEdge(@NonNull T parentData, @NonNull T childData) {
    val parentNode = getOrCreateNode(parentData);
    checkArgument(
        parentData.equals(parentNode.getData()),
        "Conflicting parent data between stored data and input data for name: %s",
        parentData.getName());

    val childNode = getOrCreateNode(childData);
    checkArgument(
        childData.equals(childNode.getData()),
        "Conflicting child data between stored data and input data for name: %s",
        childData.getName());

    initNode(parentNode);
    initNode(childNode);

    nodeMap.get(parentNode).add(childNode);
    childNode.incrementUnvisitedParents();
    return this;
  }

  private void initNode(Node<T> node) {
    if (!nodeMap.containsKey(node)) {
      nodeMap.put(node, new HashSet<>());
    }
  }

  private void internalAddEdge(@NonNull Node<T> parent, @NonNull Node<T> child) {
    nodeMap.get(parent).add(child);
    child.incrementUnvisitedParents();
  }

  // TODO: test immutability
  public MemoryGraph<T> build() {
    val memoryNodeMap = new HashMap<MemoryNode<T>, Set<MemoryNode<T>>>();

    final Map<String, MemoryNode<T>> memoryNameMap =
        nameMap.entrySet().stream()
            .collect(toUnmodifiableMap(Map.Entry::getKey, x -> MemoryNode.of(x.getValue())));

    for (val e : nodeMap.entrySet()) {
      val parentNode = e.getKey();

      val parentMemoryNode = memoryNameMap.get(parentNode.getData().getName());
      val childMemoryNodes =
          e.getValue().stream()
              .map(Node::getData)
              .map(Nameable::getName)
              .map(memoryNameMap::get)
              .collect(toUnmodifiableSet());
      memoryNodeMap.put(parentMemoryNode, childMemoryNodes);
    }
    return new MemoryGraph<>(memoryNameMap, Map.copyOf(memoryNodeMap));
  }

  private Node<T> getOrCreateNode(@NonNull T t) {
    return findNodeByName(t.getName())
        .orElseGet(
            () -> {
              val n = Node.of(t);
              this.nameMap.put(t.getName(), n);
              return n;
            });
  }
}
