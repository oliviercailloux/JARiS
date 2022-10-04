package io.github.oliviercailloux.jaris.collections;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Verify;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.PredecessorsFunction;
import com.google.common.graph.SuccessorsFunction;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphUtils {

  public static <E, F extends E> MutableGraph<E> asGraph(Set<F> roots,
      SuccessorsFunction<F> successorsFunction, PredecessorsFunction<F> predecessorsFunction) {
    checkNotNull(roots);
    checkNotNull(successorsFunction);
    checkNotNull(predecessorsFunction);
    checkArgument(roots.stream().noneMatch(t -> t == null));

    final Queue<F> toConsider = new LinkedList<>(roots);
    final Set<F> seen = new HashSet<>(roots);

    final MutableGraph<E> mutableGraph = GraphBuilder.directed().allowsSelfLoops(true).build();
    while (!toConsider.isEmpty()) {
      final F current = toConsider.remove();
      Verify.verify(current != null);
      mutableGraph.addNode(current);
      final Iterable<? extends F> successors = successorsFunction.successors(current);
      checkArgument(successors != null);
      for (F successor : successors) {
        checkArgument(successor != null);
        mutableGraph.putEdge(current, successor);
        if (!seen.contains(successor)) {
          toConsider.add(successor);
          seen.add(successor);
        }
      }
      final Iterable<? extends F> predecessors = predecessorsFunction.predecessors(current);
      checkArgument(predecessors != null);
      for (F predecessor : predecessors) {
        checkArgument(predecessor != null);
        mutableGraph.putEdge(predecessor, current);
        if (!seen.contains(predecessor)) {
          toConsider.add(predecessor);
          seen.add(predecessor);
        }
      }
    }
    return mutableGraph;
  }

  public static <E> MutableGraph<E> asGraph(List<E> elements) {
    final MutableGraph<E> builder = GraphBuilder.directed().build();
    final ListIterator<E> iterator = elements.listIterator();
    final PeekingIterator<E> peekingIterator = Iterators.peekingIterator(iterator);
    while (peekingIterator.hasNext()) {
      final E e1 = peekingIterator.next();
      if (peekingIterator.hasNext()) {
        final E e2 = peekingIterator.peek();
        builder.putEdge(e1, e2);
      }
    }
    return builder;
  }

  /**
   * The resulting graph has the same topology than the original one iff their number of nodes is
   * equal iff the given mapping is injective.
   * <p>
   * If the given mapping is not injective, the resulting graph could acquire loops.
   */
  public static <E, F> MutableGraph<F> transformed(Graph<E> graph, Map<E, F> mapping) {
    final GraphBuilder<Object> startBuilder =
        graph.isDirected() ? GraphBuilder.directed() : GraphBuilder.undirected();
    startBuilder.allowsSelfLoops(graph.allowsSelfLoops());
    final MutableGraph<F> builder = startBuilder.build();
    final Set<E> nodes = graph.nodes();
    for (E node : nodes) {
      builder.addNode(checkNotNull(mapping.get(node)));
    }
    final Set<EndpointPair<E>> edges = graph.edges();
    for (EndpointPair<E> edge : edges) {
      final F source = checkNotNull(mapping.get(edge.source()));
      final F target = checkNotNull(mapping.get(edge.target()));
      builder.putEdge(source, target);
    }
    return builder;
  }

  /**
   * From jbduncan at https://github.com/jrtom/jung/pull/174
   */
  public static <N> Iterable<N> topologicallySortedNodes(Graph<N> graph) {
    return new TopologicallySortedNodes<>(graph);
  }

  private static class TopologicallySortedNodes<N> extends AbstractSet<N> {
    private final Graph<N> graph;

    private TopologicallySortedNodes(Graph<N> graph) {
      this.graph = checkNotNull(graph, "graph");
    }

    @Override
    public UnmodifiableIterator<N> iterator() {
      return new TopologicalOrderIterator<>(graph);
    }

    @Override
    public int size() {
      return graph.nodes().size();
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }
  }

  private static class TopologicalOrderIterator<N> extends AbstractIterator<N> {
    private final Graph<N> graph;
    private final Queue<N> roots;
    private final Map<N, Integer> nonRootsToInDegree;

    private TopologicalOrderIterator(Graph<N> graph) {
      this.graph = checkNotNull(graph, "graph");
      this.roots = graph.nodes().stream().filter(node -> graph.inDegree(node) == 0)
          .collect(Collectors.toCollection(ArrayDeque::new));
      this.nonRootsToInDegree = graph.nodes().stream().filter(node -> graph.inDegree(node) > 0)
          .collect(Collectors.toMap(node -> node, graph::inDegree, (a, b) -> a, HashMap::new));
    }

    @Override
    protected N computeNext() {
      // Kahn's algorithm
      if (!roots.isEmpty()) {
        N next = roots.remove();
        for (N successor : graph.successors(next)) {
          int newInDegree = nonRootsToInDegree.get(successor) - 1;
          nonRootsToInDegree.put(successor, newInDegree);
          if (newInDegree == 0) {
            nonRootsToInDegree.remove(successor);
            roots.add(successor);
          }
        }
        return next;
      }
      checkState(nonRootsToInDegree.isEmpty(), "graph has at least one cycle");
      return endOfData();
    }
  }
}
