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

public class ThrowingStreamTests {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(ThrowingStreamTests.class);

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
		final ThrowingStream<Integer, IOException> ts = ThrowingStream.wrapping(ImmutableSet.of(1, 2, 4).stream());
		assertThrows(NoSuchFileException.class, () -> ts.allMatch(ThrowingStreamTests::oneOrThrows));
	}

	@Test
	void testDoesNotThrow() throws Exception {
		final ThrowingStream<Integer, IOException> ts = ThrowingStream.wrapping(ImmutableSet.of(1, 2, 4).stream());
		assertFalse(ts.allMatch(ThrowingStreamTests::oneOrFalse));
	}

	@Test
	void testDistinctThrows() throws Exception {
		final ThrowingStream<Integer, IOException> ts = ThrowingStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
		assertThrows(NoSuchFileException.class, () -> ts.distinct().allMatch(ThrowingStreamTests::oneOrThrows));
	}

	@Test
	void testDistinctDoesNotThrow() throws Exception {
		final ThrowingStream<Integer, IOException> ts = ThrowingStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
		assertFalse(ts.allMatch(ThrowingStreamTests::oneOrFalse));
	}

	@Test
	void testFilterThrows() throws Exception {
		final ThrowingStream<Integer, IOException> ts = ThrowingStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
		final ThrowingStream<Integer, IOException> filtered = ts.filter(ThrowingStreamTests::oneOrThrows);
		final ThrowingStream<Integer, IOException> filteredDistinct = filtered.distinct();
		assertThrows(NoSuchFileException.class, () -> filteredDistinct.collect​(ImmutableList.toImmutableList()));
	}

	@Test
	void testFilterDoesNotThrow() throws Exception {
		final ThrowingStream<Integer, IOException> ts = ThrowingStream.wrapping(ImmutableList.of(1, 2, 1, 4).stream());
		final ThrowingStream<Integer, IOException> filtered = ts.filter(ThrowingStreamTests::oneOrFalse);
		final ImmutableList<Integer> result = filtered.distinct().collect​(ImmutableList.toImmutableList());
		assertEquals(ImmutableList.of(1), result);
	}
}
