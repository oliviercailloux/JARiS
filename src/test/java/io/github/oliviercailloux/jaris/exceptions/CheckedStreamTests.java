package io.github.oliviercailloux.jaris.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class CheckedStreamTests {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckedStreamTests.class);

	private static boolean oneOrThrows(Integer input) throws IOException {
		switch (input) {
		case 1:
			return true;
		default:
			throw new NoSuchFileException("file");
		}
	}

	private static boolean oneOrFalse(Integer input) {
		return Integer.valueOf(1).equals(input);
	}

	@Test
	void testThrows() throws Exception {
		final CheckedStream<Integer, IOException> ts = CheckedStream.wrapping(ImmutableSet.of(1, 2, 4).stream());
		assertThrows(NoSuchFileException.class, () -> ts.allMatch(CheckedStreamTests::oneOrThrows));
	}

	@Test
	void testDoesNotThrow() throws Exception {
		final CheckedStream<Integer, IOException> ts = CheckedStream.wrapping(ImmutableSet.of(1, 2, 4).stream());
		assertFalse(ts.allMatch(CheckedStreamTests::oneOrFalse));
	}

	@Test
	void testDistinctThrows() throws Exception {
		final CheckedStream<Integer, IOException> ts = CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
		assertThrows(NoSuchFileException.class, () -> ts.distinct().allMatch(CheckedStreamTests::oneOrThrows));
	}

	@Test
	void testDistinctDoesNotThrow() throws Exception {
		final CheckedStream<Integer, IOException> ts = CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
		assertFalse(ts.allMatch(CheckedStreamTests::oneOrFalse));
	}

	@Test
	void testFilterThrows() throws Exception {
		final CheckedStream<Integer, IOException> ts = CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
		final CheckedStream<Integer, IOException> filtered = ts.filter(CheckedStreamTests::oneOrThrows);
		final CheckedStream<Integer, IOException> filteredDistinct = filtered.distinct();
		assertThrows(NoSuchFileException.class, () -> filteredDistinct.collect​(ImmutableList.toImmutableList()));
	}

	@Test
	void testFilterDoesNotThrow() throws Exception {
		final CheckedStream<Integer, IOException> ts = CheckedStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
		final CheckedStream<Integer, IOException> filtered = ts.filter(CheckedStreamTests::oneOrFalse);
		final ImmutableList<Integer> result = filtered.distinct().collect​(ImmutableList.toImmutableList());
		assertEquals(ImmutableList.of(1), result);
	}
}
