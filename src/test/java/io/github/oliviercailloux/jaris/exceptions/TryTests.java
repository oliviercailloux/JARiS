package io.github.oliviercailloux.jaris.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class TryTests {
  @Test
  void testSuccess() throws Exception {
    final UnsupportedOperationException runtimeExc = new UnsupportedOperationException();
    final IOException cause = new IOException();
    final Try<Integer, Exception> t = Try.success(1);

    assertEquals(t, Try.get(() -> 1));

    assertEquals(Try.success(1), t.andRun(TryVoid.success()::orThrow));
    assertEquals(Try.failure(cause), t.andRun(TryVoid.failure(cause)::orThrow));

    assertEquals(Try.success(3), t.and(Try.success(2), (i1, i2) -> i1 + i2));
    assertEquals(Try.failure(cause),
        t.and(Try.<Integer, Exception>failure(cause), TryTests::mergeAdding));
    assertThrows(IOException.class, () -> t.and(Try.success(2), TryTests::mergeThrowing));

    assertEquals(Try.success(1), t.andConsume(i -> {
    }));
    assertEquals(Try.failure(cause), t.andConsume(i -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.andConsume(i -> {
      throw runtimeExc;
    }));

    assertEquals(Try.success(1), t.andRun(() -> {
    }));
    assertEquals(Try.failure(cause), t.andRun(() -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.andRun(() -> {
      throw runtimeExc;
    }));

    assertEquals(Try.success(5), t.flatMap(i -> i + 4));
    assertEquals(Try.failure(cause), t.flatMap(i -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.flatMap(i -> {
      throw runtimeExc;
    }));

    assertFalse(t.isFailure());
    assertTrue(t.isSuccess());

    assertEquals(Optional.of(1), t.orConsumeCause(i -> {
    }));
    assertEquals(Optional.of(1), t.orConsumeCause(i -> {
      throw cause;
    }));
    assertEquals(Optional.of(1), t.orConsumeCause(i -> {
      throw runtimeExc;
    }));

    assertEquals(Try.success(1), t.orGet(() -> 6, (e1, e2) -> cause));
    assertEquals(Try.success(1), t.orGet(() -> {
      throw cause;
    }, (e1, e2) -> cause));
    assertEquals(Try.success(1), t.orGet(() -> {
      throw runtimeExc;
    }, (e1, e2) -> cause));

    assertEquals(1, t.orMapCause(e -> 8));
    assertEquals(1, t.orMapCause(e -> {
      throw cause;
    }));
    assertEquals(1, t.orMapCause(e -> {
      throw runtimeExc;
    }));

    assertEquals(1, t.orThrow());

    assertEquals(TryVoid.success(), t.toTryVoid());
  }

  @Test
  void testFailure() throws Exception {
    final UnsupportedOperationException runtimeExc = new UnsupportedOperationException();
    final IOException cause = new IOException();
    final Try<Integer, IOException> t = Try.failure(cause);

    assertEquals(t, Try.get(() -> {
      throw cause;
    }));

    assertEquals(Try.failure(cause), t.andRun(TryVoid.<IOException>success()::orThrow));
    assertEquals(Try.failure(cause), t.andRun(TryVoid.failure(cause)::orThrow));

    assertEquals(Try.failure(cause), t.and(Try.success(2), (i1, i2) -> i1 + i2));
    assertEquals(Try.failure(cause),
        t.and(Try.<Integer, IOException>failure(cause), TryTests::mergeAdding));
    assertEquals(Try.failure(cause), t.and(Try.success(2), TryTests::mergeThrowing));

    assertEquals(Try.failure(cause), t.andConsume(i -> {
    }));
    assertEquals(Try.failure(cause), t.andConsume(i -> {
      throw cause;
    }));
    assertEquals(Try.failure(cause), t.andConsume(i -> {
      throw runtimeExc;
    }));

    assertEquals(Try.failure(cause), t.andRun(() -> {
    }));
    assertEquals(Try.failure(cause), t.andRun(() -> {
      throw cause;
    }));
    assertEquals(Try.failure(cause), t.andRun(() -> {
      throw runtimeExc;
    }));

    assertEquals(Try.failure(cause), t.flatMap(i -> i + 4));
    assertEquals(Try.failure(cause), t.flatMap(i -> {
      throw cause;
    }));
    assertEquals(Try.failure(cause), t.flatMap(i -> {
      throw runtimeExc;
    }));

    assertTrue(t.isFailure());
    assertFalse(t.isSuccess());

    assertEquals(Optional.empty(), t.orConsumeCause(i -> {
    }));
    assertThrows(IOException.class, () -> t.orConsumeCause(i -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.orConsumeCause(i -> {
      throw runtimeExc;
    }));

    assertEquals(Try.success(6), t.orGet(() -> 6, (e1, e2) -> cause));
    final MalformedParametersException mergedCause = new MalformedParametersException();
    final FileNotFoundException cause2 = new FileNotFoundException();
    assertEquals(Try.failure(mergedCause), t.orGet(() -> {
      throw cause2;
    }, (e1, e2) -> e1.equals(cause) && e2.equals(cause2) ? mergedCause : runtimeExc));
    assertThrows(UnsupportedOperationException.class, () -> t.orGet(() -> {
      throw runtimeExc;
    }, (e1, e2) -> cause));

    assertEquals(8, t.orMapCause(e -> 8));
    assertThrows(IOException.class, () -> t.orMapCause(e -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.orMapCause(e -> {
      throw runtimeExc;
    }));

    assertThrows(IOException.class, () -> t.orThrow());

    assertEquals(TryVoid.failure(cause), t.toTryVoid());
    assertEquals(t, t.toTryVoid());
  }

  static int mergeAdding(int i1, int i2) {
    return i1 + i2;
  }

  @SuppressWarnings("unused")
  static int mergeThrowing(int i1, int i2) throws IOException {
    throw new IOException();
  }
}
