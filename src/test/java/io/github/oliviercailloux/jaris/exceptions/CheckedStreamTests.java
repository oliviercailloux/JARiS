package io.github.oliviercailloux.jaris.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CheckedStreamTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckedStreamTests.class);

  private static boolean oneOrThrows(Integer input) throws IOException {
    switch (input) {
      case 1:
        return true;
      default:
        throw new NoSuchFileException(input.toString());
    }
  }

  private static boolean oneOrThrows(String input) throws IOException {
    switch (input) {
      case "1":
        return true;
      default:
        throw new IOException(input);
    }
  }

  private static boolean oneOrFalse(Integer input) {
    return Integer.valueOf(1).equals(input);
  }

  private static String toStringOneOrThrows(Integer input) throws IOException {
    switch (input) {
      case 1:
        return "1";
      default:
        throw new IOException(input.toString());
    }
  }

  private static Stream<String> toStreamOfStringsOneOrThrows(Integer input) throws IOException {
    switch (input) {
      case 1:
        return Stream.of("1");
      default:
        throw new IOException(input.toString());
    }
  }

  private static Stream<String> toStreamOfStrings(Integer input) {
    return Stream.generate(() -> input.toString()).limit(input);
  }

  @Test
  void testAllMatch() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableSet.of(1, 2, 4).stream());
    assertFalse(ts.allMatch(CheckedStreamTests::oneOrFalse));
  }

  @Test
  void testAllMatchThrows() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableSet.of(1, 2, 4).stream());
    assertThrows(NoSuchFileException.class, () -> ts.allMatch(CheckedStreamTests::oneOrThrows));
  }

  @Test
  void testDistinctAllMatch() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    assertFalse(ts.allMatch(CheckedStreamTests::oneOrFalse));
  }

  @Test
  void testDistinctAllMatchThrows() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    assertThrows(NoSuchFileException.class,
        () -> ts.distinct().allMatch(CheckedStreamTests::oneOrThrows));
  }

  @Test
  void testSpecify() throws IOException {
    // CheckedStream<Integer, IOException> distinct = CheckedStream.<Integer, IOException>wrapping(ImmutableList.of(1, 2, 1, 4).stream()).filter(CheckedStreamTests::oneOrFalse).distinct();
    CheckedStream<Integer, IOException> distinct = CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream()).<IOException>specify().filter(CheckedStreamTests::oneOrFalse).distinct();
    assertEquals(ImmutableList.of(1), distinct.collect(ImmutableList.toImmutableList()));
  }

  @Test
  void testFilter() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    final CheckedStream<Integer, IOException> filtered = ts.filter(CheckedStreamTests::oneOrFalse);
    final ImmutableList<Integer> result =
        filtered.distinct().collect(ImmutableList.toImmutableList());
    assertEquals(ImmutableList.of(1), result);
  }

  @Test
  void testFilterCount() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    final CheckedStream<Integer, IOException> filtered = ts.filter(CheckedStreamTests::oneOrFalse);
    assertEquals(1, filtered.distinct().count());
  }

  @Test
  void testFilterThrows() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    final CheckedStream<Integer, IOException> filtered = ts.filter(CheckedStreamTests::oneOrThrows);
    final CheckedStream<Integer, IOException> filteredDistinct = filtered.distinct();
    assertThrows(NoSuchFileException.class,
        () -> filteredDistinct.collect(ImmutableList.toImmutableList()));
  }

  @Test
  void testLimitFilter() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    final CheckedStream<Integer, IOException> filtered =
        ts.limit(1L).filter(CheckedStreamTests::oneOrThrows);
    assertEquals(1L, filtered.count());
  }

  @Test
  void testLimitFilterThrows() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    final CheckedStream<Integer, IOException> filtered =
        ts.limit(2L).filter(CheckedStreamTests::oneOrThrows);
    assertThrows(NoSuchFileException.class, () -> filtered.count());
  }

  @Test
  void testMap() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    final CheckedStream<String, IOException> mapped = ts.map(i -> i.toString());
    final CheckedStream<String, IOException> mappedFiltered = mapped.map(s -> s.replace("1", "15"));
    assertEquals(ImmutableList.of("15", "2", "15", "4"),
        mappedFiltered.collect(ImmutableList.toImmutableList()));
  }

  @Test
  void testMapThrows() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    final CheckedStream<String, IOException> mapped =
        ts.map(CheckedStreamTests::toStringOneOrThrows);
    /*
     * Note that counting the elements here may not throw anything, as per the contract of #count().
     */
    assertThrows(IOException.class, () -> mapped.collect(ImmutableList.toImmutableList()));
  }

  @Test
  void testMapThenThrows() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    final CheckedStream<String, IOException> mapped = ts.map(i -> i.toString());
    final CheckedStream<String, IOException> mappedFiltered =
        mapped.filter(CheckedStreamTests::oneOrThrows);
    assertThrows(IOException.class, () -> mappedFiltered.count());
  }

  @Test
  void testFlatMap() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    final CheckedStream<String, IOException> mapped =
        ts.flatMap(CheckedStreamTests::toStreamOfStrings);
    assertEquals(ImmutableList.of("1", "2", "2", "1", "4", "4", "4", "4"),
        mapped.collect(ImmutableList.toImmutableList()));
  }

  @Test
  void testFlatMapThrows() throws Exception {
    final CheckedStream<Integer, IOException> ts =
        CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
    final CheckedStream<String, IOException> mapped =
        ts.flatMap(CheckedStreamTests::toStreamOfStringsOneOrThrows);
    assertThrows(IOException.class, () -> mapped.count());
  }
}
