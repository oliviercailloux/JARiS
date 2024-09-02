package io.github.oliviercailloux.jaris.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import io.github.oliviercailloux.jaris.graphs.GraphUtils;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class GraphUtilsTests {

  @Test
  void testSetAsGraph() {
    final ImmutableMap<Integer, ImmutableSet<Integer>> succs = ImmutableMap.of(1,
        ImmutableSet.of(2, 3), 2, ImmutableSet.of(4), 3, ImmutableSet.of(), 4, ImmutableSet.of());
    final ImmutableMap<Integer, ImmutableSet<Integer>> preds = ImmutableMap.of(1, ImmutableSet.of(),
        2, ImmutableSet.of(1), 3, ImmutableSet.of(), 4, ImmutableSet.of(3));
    final MutableGraph<Integer> expected = GraphBuilder.directed().build();
    expected.putEdge(1, 2);
    expected.putEdge(1, 3);
    expected.putEdge(2, 4);
    expected.putEdge(3, 4);
    assertEquals(expected, GraphUtils.asGraph(ImmutableSet.of(1), succs::get, preds::get));
  }

  @Test
  void testSetAsGraphLooping() {
    final ImmutableMap<Integer, ImmutableSet<Integer>> succs =
        ImmutableMap.of(1, ImmutableSet.of(2), 2, ImmutableSet.of());
    final ImmutableMap<Integer, ImmutableSet<Integer>> preds =
        ImmutableMap.of(1, ImmutableSet.of(1), 2, ImmutableSet.of());
    final MutableGraph<Integer> expected = GraphBuilder.directed().allowsSelfLoops(true).build();
    expected.putEdge(1, 1);
    expected.putEdge(1, 2);
    assertEquals(expected, GraphUtils.asGraph(ImmutableSet.of(1), succs::get, preds::get));
  }

  @Test
  void testSetAsGraphWithoutEdges() {
    final ImmutableMap<Integer, ImmutableSet<Integer>> succs =
        ImmutableMap.of(1, ImmutableSet.of(), 2, ImmutableSet.of());
    final MutableGraph<Integer> expected = GraphBuilder.directed().allowsSelfLoops(true).build();
    expected.addNode(1);
    expected.addNode(2);
    assertEquals(expected, GraphUtils.asGraph(ImmutableSet.of(1, 2), succs::get, succs::get));
  }

  @Test
  void testSort() {
    final MutableGraph<Integer> graph = GraphBuilder.directed().build();
    graph.putEdge(3, 4);
    graph.putEdge(1, 3);
    graph.putEdge(1, 2);
    graph.putEdge(2, 4);

    final Iterator<Integer> iterator = GraphUtils.topologicallySortedNodes(graph).iterator();
    final int it1 = iterator.next();
    final int it2 = iterator.next();
    final int it3 = iterator.next();
    final int it4 = iterator.next();
    assertFalse(iterator.hasNext());

    assertEquals(1, it1);
    assertEquals(ImmutableSet.of(2, 3), ImmutableSet.of(it2, it3));
    assertEquals(4, it4);
  }
}
