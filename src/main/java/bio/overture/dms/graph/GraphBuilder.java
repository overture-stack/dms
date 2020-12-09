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

  public GraphBuilder<T> addNode(@NonNull T data) {
    internalAddNode(data);
    return this;
  }

  private Node<T> internalAddNode(T data) {
    val node = getOrCreateNode(data);
    checkArgument(
        data.equals(node.getData()),
        "Conflicting data between stored data and input data for name: %s",
        data.getName());
    initNode(node);
    return node;
  }

  public GraphBuilder<T> addEdge(@NonNull T parentData, @NonNull T childData) {
    val parentNode = internalAddNode(parentData);
    val childNode = internalAddNode(childData);

    connectionMap.get(parentNode).add(childNode);
    childNode.incrementUnvisitedParents();
    return this;
  }

  private void initNode(Node<T> node) {
    if (!connectionMap.containsKey(node)) {
      connectionMap.put(node, new HashSet<>());
    }
  }

  private void internalAddEdge(@NonNull Node<T> parent, @NonNull Node<T> child) {
    connectionMap.get(parent).add(child);
    child.incrementUnvisitedParents();
  }

  // TODO: test immutability
  public MemoryGraph<T> build() {
    val memoryConnectionMap = new HashMap<MemoryNode<T>, Set<MemoryNode<T>>>();

    final Map<String, MemoryNode<T>> memoryNodeMap =
        nodeMap.entrySet().stream()
            .collect(toUnmodifiableMap(Map.Entry::getKey, x -> MemoryNode.of(x.getValue())));

    for (val e : connectionMap.entrySet()) {
      val parentNode = e.getKey();

      val parentMemoryNode = memoryNodeMap.get(parentNode.getData().getName());
      val childMemoryNodes =
          e.getValue().stream()
              .map(Node::getData)
              .map(Nameable::getName)
              .map(memoryNodeMap::get)
              .collect(toUnmodifiableSet());
      memoryConnectionMap.put(parentMemoryNode, childMemoryNodes);
    }
    return new MemoryGraph<>(memoryNodeMap, Map.copyOf(memoryConnectionMap));
  }

  private Node<T> getOrCreateNode(@NonNull T t) {
    return findNodeByName(t.getName())
        .orElseGet(
            () -> {
              val n = Node.of(t);
              this.nodeMap.put(t.getName(), n);
              return n;
            });
  }
}
