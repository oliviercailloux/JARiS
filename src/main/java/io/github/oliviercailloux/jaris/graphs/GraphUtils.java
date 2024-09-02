package io.github.oliviercailloux.jaris.graphs;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Verify;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.PredecessorsFunction;
import com.google.common.graph.SuccessorsFunction;
import io.github.oliviercailloux.jaris.throwing.TFunction;
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

  /**
   * Returns a mutable graph corresponding to the given nodes and reachability relations.
   * <p>
   * More precisely, this returns a mutable graph containing as nodes:
   * <ul>
   * <li>the given roots <em>R</em>, union</li>
   * <li>the transitive closure of the given successors function on <em>R</em>, union</li>
   * <li>the transitive closure of the given predecessors function on <em>R</em>;</li>
   * </ul>
   * and as edges all pairs of nodes <em>(a, b)</em> such that <em>b</em> is a successor of
   * <em>a</em> (according to the given successors function’s
   * {@link SuccessorsFunction#successors(Object) successors} method) or <em>a</em> is a direct
   * predecessor of <em>b</em> (according to the given predecessors function’s
   * {@link PredecessorsFunction#predecessors(Object) predecessors} method).
   * <p>
   * The returned graph allows self loops.
   *
   * @param <E> the type of nodes contained in the returned graph.
   * @param <F> the type of nodes used as roots and in the successors and predecessors functions.
   * @param roots an initial set of nodes
   * @param successorsFunction the successors function
   * @param predecessorsFunction the predecessors function
   * @return a mutable graph
   */
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

  /**
   * Returns a copy of the given list representing the “has-as-next-element” relation. For example,
   * given the list <em>(a, b, c)</em>, this method returns the graph of nodes <em>{a, b, c}</em>
   * and edges <em>{(a, b), (b, c)}</em>. Given the list <em>(a, b, a, b)</em>, this method returns
   * the graph of nodes <em>{a, b}</em> and edges <em>{(a, b), (b, a)}</em>.
   * <p>
   * The returned graph allows self loops. It contains loops iff the given list contains the same
   * element at consecutive positions.
   * <p>
   * The number of nodes in the returned graph is the number of distinct elements in the given list.
   * If the given list contains no duplicate elements, the number of edges is the size of the given
   * list minus one, and mathematically speaking, the returned graph is a tree and is a list.
   *
   * @param <E> the type of nodes in the graph.
   * @param elements the list to be transformed to a graph
   * @return a mutable graph
   */
  public static <E> MutableGraph<E> asGraph(List<? extends E> elements) {
    final MutableGraph<E> builder = GraphBuilder.directed().allowsSelfLoops(true).build();
    final ListIterator<? extends E> iterator = elements.listIterator();
    final PeekingIterator<? extends E> peekingIterator = Iterators.peekingIterator(iterator);
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
   * Computes the transitive closure of the given graph, understood as
   * <a href="https://github.com/google/guava/issues/2778">not implying reflexivity</a>.
   *
   * @param <E> the nodes
   * @param graph the graph
   * @return the transitive closure
   */
  public static <E> MutableGraph<E> transitiveClosure(Graph<E> graph) {
    final Graph<E> reflCl = Graphs.transitiveClosure(graph);
    final MutableGraph<E> mutable = Graphs.copyOf(reflCl);
    graph.nodes().stream().filter(n -> !graph.successors(n).contains(n))
        .forEach(n -> mutable.removeEdge(n, n));
    return mutable;
  }

  /**
   * Returns a transformation of a graph that uses a given mapping.
   * <p>
   * Each edge <em>(a, b)</em> of the given graph becomes an edge <em>(a’, b’)</em> where
   * <em>a’</em> is the value associated to <em>a</em> by the given mapping; and <em>b’</em> is the
   * valued associated to <em>b</em>.
   * <p>
   * For example, given:
   * </p>
   * <ul>
   * <li>the graph with nodes <em>{a, b, c, d}</em> and edges <em>{(a, c), (b, d)}</em>, and</li>
   * <li>the mapping <em>{(a, a), (b, b), (c, e), (d, e)}</em>,</li>
   * </ul>
   * <p>
   * this method returns the graph having nodes <em>{a, b, e}</em> and edges <em>{(a, e), (b,
   * e)}</em>.
   * <p>
   * The resulting graph has the same topology than the original one iff their number of nodes is
   * equal iff the given mapping is injective.
   * <p>
   * If the given mapping is not injective, the resulting graph could acquire loops.
   * <p>
   * The resulting graph allows loops iff the given one does.
   * </p>
   *
   * @param <E> the type of nodes in the source graph
   * @param <F> the type of nodes in the returned graph
   * @param graph the source
   * @param mapping the mapping
   * @return a mutable graph
   * @throws IllegalArgumentException if the given graph does not allow loops and the mapping is not
   *         injective or if some node of the given graph is absent from the given mapping or
   *         associated to a {@code null} value.
   */
  public static <E, F> MutableGraph<F> transformed(Graph<E> graph,
      Map<? super E, ? extends F> mapping) {
    final GraphBuilder<Object> startBuilder =
        graph.isDirected() ? GraphBuilder.directed() : GraphBuilder.undirected();
    startBuilder.allowsSelfLoops(graph.allowsSelfLoops());
    final MutableGraph<F> builder = startBuilder.build();
    final Set<E> nodes = graph.nodes();
    for (E node : nodes) {
      final F target = mapping.get(node);
      checkArgument(target != null);
      builder.addNode(target);
    }
    final Set<EndpointPair<E>> edges = graph.edges();
    for (EndpointPair<E> edge : edges) {
      final F source = mapping.get(edge.source());
      checkArgument(source != null);
      final F target = mapping.get(edge.target());
      checkArgument(target != null);
      builder.putEdge(source, target);
    }
    return builder;
  }

  /**
   * Returns a transformation of a graph that uses a given mapping.
   * <p>
   * Each edge <em>(a, b)</em> of the given graph becomes an edge <em>(a’, b’)</em> where
   * <em>a’</em> is the value associated to <em>a</em> by the given mapping; and <em>b’</em> is the
   * valued associated to <em>b</em>.
   * <p>
   * For example, given:
   * </p>
   * <ul>
   * <li>the graph with nodes <em>{a, b, c, d}</em> and edges <em>{(a, c), (b, d)}</em>, and</li>
   * <li>the mapping <em>{(a, a), (b, b), (c, e), (d, e)}</em>,</li>
   * </ul>
   * <p>
   * this method returns the graph having nodes <em>{a, b, e}</em> and edges <em>{(a, e), (b,
   * e)}</em>.
   * <p>
   * The resulting graph has the same topology than the original one iff their number of nodes is
   * equal iff the given mapping is injective.
   * <p>
   * If the given mapping is not injective, the resulting graph could acquire loops.
   * <p>
   * The resulting graph allows loops iff the given one does.
   * </p>
   *
   * @param <E> the type of nodes in the source graph
   * @param <F> the type of nodes in the returned graph
   * @param graph the source
   * @param mapping the mapping
   * @return a mutable graph
   * @throws IllegalArgumentException if the given graph does not allow loops and the mapping is not
   *         injective or if some node of the given graph is absent from the given mapping or
   *         associated to a {@code null} value.
   */
  public static <E, F, X extends Exception> MutableGraph<F> transform(Graph<E> graph,
      TFunction<? super E, ? extends F, X> mapping) throws X {
    final GraphBuilder<Object> startBuilder =
        graph.isDirected() ? GraphBuilder.directed() : GraphBuilder.undirected();
    startBuilder.allowsSelfLoops(graph.allowsSelfLoops());
    final MutableGraph<F> builder = startBuilder.build();
    final Set<E> nodes = graph.nodes();
    for (E node : nodes) {
      final F target = mapping.apply(node);
      checkArgument(target != null);
      builder.addNode(target);
    }
    final Set<EndpointPair<E>> edges = graph.edges();
    for (EndpointPair<E> edge : edges) {
      final F source = mapping.apply(edge.source());
      checkArgument(source != null);
      final F target = mapping.apply(edge.target());
      checkArgument(target != null);
      builder.putEdge(source, target);
    }
    return builder;
  }

  /**
   * From <a href="https://github.com/jrtom/jung/pull/174">jbduncan</a>.
   */
  public static <N> ImmutableSet<N> topologicallySortedNodes(Graph<N> graph) {
    return ImmutableSet.copyOf(new TopologicalOrderIterator<>(graph));
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

  /**
   * Should not be instanciated.
   */
  private GraphUtils() {}
}
