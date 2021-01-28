package io.github.oliviercailloux.jaris.exceptions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class ThrowingStreamTests {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(ThrowingStreamTests.class);

	private static boolean oneOrThrows(Integer input) throws IOException {
		switch (input) {
		case 1:
			return true;
		default:
			throw new IOException();
		}
	}

	private static boolean oneOrFalse(Integer input) {
		return Integer.valueOf(1).equals(input);
	}

	@Test
	void testThrows() throws Exception {
		final ThrowingStream<Integer, IOException> ts = ThrowingStream.wrapping(ImmutableSet.of(1, 2, 4).stream());
		assertThrows(IOException.class, () -> ts.allMatch(ThrowingStreamTests::oneOrThrows));
	}

	@Test
	void testDoesNotThrow() throws Exception {
		final ThrowingStream<Integer, IOException> ts = ThrowingStream.wrapping(ImmutableSet.of(1, 2, 4).stream());
		assertFalse(ts.allMatch(ThrowingStreamTests::oneOrFalse));
	}
}
