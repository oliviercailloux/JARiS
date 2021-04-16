package io.github.oliviercailloux.jaris.exceptions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.VerifyException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TryTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(TryTests.class);

  @Test
  void testSuccess() {
    final UnsupportedOperationException runtimeExc = new UnsupportedOperationException();
    final IOException cause = new IOException();
    final Try<Integer, Exception> t = Try.success(1);

    assertEquals(t, Try.get(() -> 1));

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

    assertEquals(Try.success(1), t.andRun(TryVoid.success()::orThrow));
    assertEquals(Try.failure(cause), t.andRun(TryVoid.failure(cause)::orThrow));

    assertEquals(Try.success(1), t.andRun(() -> {
    }));
    assertEquals(Try.failure(cause), t.andRun(() -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.andRun(() -> {
      throw runtimeExc;
    }));

    assertEquals(Try.success(5), t.map(i -> Try.get(() -> i + 4), f -> t));
    assertEquals(Try.success(5), t.andApply(i -> i + 4));
    assertEquals(Try.failure(cause), t.andApply(i -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.andApply(i -> {
      throw runtimeExc;
    }));

    assertFalse(t.isFailure());
    assertTrue(t.isSuccess());

    assertEquals(Optional.of(1), t.orConsumeCause(i -> {
    }));
    try {
      assertEquals(Optional.of(1), t.orConsumeCause(i -> {
        throw cause;
      }));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    assertEquals(Optional.of(1), t.orConsumeCause(i -> {
      throw runtimeExc;
    }));

    assertEquals(Try.success(1), t.or(Try.success(6)::orThrow, (e1, e2) -> cause));
    assertEquals(Try.success(1),
        t.or(Try.<Integer, IOException>failure(cause)::orThrow, (e1, e2) -> cause));

    assertEquals(Try.success(1), t.or(() -> 6, (e1, e2) -> cause));
    assertEquals(Try.success(1), t.or(() -> {
      throw cause;
    }, (e1, e2) -> cause));
    assertEquals(Try.success(1), t.or(() -> {
      throw runtimeExc;
    }, (e1, e2) -> cause));

    assertEquals(1, t.orMapCause(e -> 8));
    try {
      assertEquals(1, t.orMapCause(e -> {
        throw cause;
      }));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    assertEquals(1, t.orMapCause(e -> {
      throw runtimeExc;
    }));

    try {
      assertEquals(1, t.orThrow());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    try {
      assertEquals(1, t.orThrow(Function.identity()));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    try {
      assertEquals(1, t.orThrow(e -> cause));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    assertNotEquals(TryVoid.success(), t);

    /* Just to check whether it is overridden (otherwise it starts with the full name). */
    assertTrue(t.toString().startsWith("Try"), t.toString());
  }

  /**
   * Just to illustrate the good ol’ success → value / else → exc approach.
   */
  @Test
  void testGet() {
    final Try<Integer, Exception> t = Try.success(1);
    if (t.isSuccess()) {
      final int value = t.orThrow(VerifyException::new);
      LOGGER.info("Success: {}.", value);
    } else {
      final Exception exc = t.map(i -> {
        throw new VerifyException("Unexpected success: " + i);
      }, e -> e);
      /* Alternative: final Exception exc = t.map(i -> null, e -> e); */
      LOGGER.info("Failure: {}.", exc);
    }
  }

  @Test
  void testFailure() {
    final UnsupportedOperationException runtimeExc = new UnsupportedOperationException();
    final IOException cause = new IOException();
    final Try<Integer, IOException> t = Try.failure(cause);

    assertEquals(t, Try.get(() -> {
      throw cause;
    }));

    assertEquals(Try.failure(cause), t.and(Try.success(2), (i1, i2) -> i1 + i2));
    assertEquals(Try.failure(cause),
        t.and(Try.<Integer, IOException>failure(cause), TryTests::mergeAdding));
    try {
      assertEquals(Try.failure(cause), t.and(Try.success(2), TryTests::mergeThrowing));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    assertEquals(Try.failure(cause), t.andConsume(i -> {
    }));
    assertEquals(Try.failure(cause), t.andConsume(i -> {
      throw cause;
    }));
    assertEquals(Try.failure(cause), t.andConsume(i -> {
      throw runtimeExc;
    }));

    assertEquals(Try.failure(cause), t.andRun(TryVoid.<IOException>success()::orThrow));
    assertEquals(Try.failure(cause), t.andRun(TryVoid.failure(cause)::orThrow));

    assertEquals(Try.failure(cause), t.andRun(() -> {
    }));
    assertEquals(Try.failure(cause), t.andRun(() -> {
      throw cause;
    }));
    assertEquals(Try.failure(cause), t.andRun(() -> {
      throw runtimeExc;
    }));

    assertEquals(Try.failure(cause), t.andApply(i -> i + 4));
    assertEquals(Try.failure(cause), t.andApply(i -> {
      throw cause;
    }));
    assertEquals(Try.failure(cause), t.andApply(i -> {
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

    final FileNotFoundException cause2 = new FileNotFoundException();
    final MalformedParametersException mergedCause = new MalformedParametersException();
    assertEquals(Try.success(6), t.or(Try.success(6)::orThrow, (e1, e2) -> cause));
    assertEquals(Try.failure(mergedCause), t.or(Try.<Integer, IOException>failure(cause2)::orThrow,
        (e1, e2) -> e1.equals(cause) && e2.equals(cause2) ? mergedCause : runtimeExc));

    assertEquals(Try.success(6), t.or(() -> 6, (e1, e2) -> cause));
    assertEquals(Try.failure(mergedCause), t.or(() -> {
      throw cause2;
    }, (e1, e2) -> e1.equals(cause) && e2.equals(cause2) ? mergedCause : runtimeExc));
    assertThrows(UnsupportedOperationException.class, () -> t.or(() -> {
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
    assertThrows(IOException.class, () -> t.orThrow(Function.identity()));
    assertThrows(UnsupportedOperationException.class, () -> t.orThrow(e -> runtimeExc));

    assertEquals(t, TryVoid.failure(cause));

    assertTrue(t.toString().startsWith("Try"), t.toString());
  }

  @Test
  void testVoidSuccess() {
    final UnsupportedOperationException runtimeExc = new UnsupportedOperationException();
    final IOException cause = new IOException();
    final TryVoid<Exception> t = TryVoid.success();
    assertEquals(t, TryVoid.run(() -> {
    }));

    assertEquals(Try.success(1), t.andGet(Try.success(1)::orThrow));
    assertEquals(Try.failure(cause), t.andGet(Try.failure(cause)::orThrow));
    assertEquals(Try.success(1), t.andGet(() -> 1));
    assertEquals(Try.failure(cause), t.andGet(() -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.andGet(() -> {
      throw runtimeExc;
    }));

    assertEquals(TryVoid.success(), t.andRun(TryVoid.success()::orThrow));
    assertEquals(TryVoid.failure(cause), t.andRun(TryVoid.failure(cause)::orThrow));
    assertEquals(TryVoid.success(), t.andRun(() -> {
    }));
    assertEquals(TryVoid.failure(cause), t.andRun(() -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.andRun(() -> {
      throw runtimeExc;
    }));

    // final Consumer<Exception, IOException> c = Mockito.mock(Consumer.class);
    // t.ifFailed(c);
    // Mockito.verifyNoInteractions(c);
    assertDoesNotThrow(() -> t.ifFailed(e -> {
      throw cause;
    }));
    // assertThrows(UnsupportedOperationException.class, () -> t.ifFailed(e -> {
    // throw runtimeExc;
    // }));

    assertFalse(t.isFailure());
    assertTrue(t.isSuccess());

    assertEquals(2, t.map(() -> 2, e -> 3));
    assertThrows(IOException.class, () -> t.map(() -> {
      throw cause;
    }, e -> 3));
    assertThrows(UnsupportedOperationException.class, () -> t.map(() -> {
      throw runtimeExc;
    }, e -> 3));

    assertEquals(TryVoid.success(), t.or(() -> {
    }));
    assertEquals(TryVoid.success(), t.or(() -> {
      throw cause;
    }));
    assertEquals(TryVoid.success(), t.or(() -> {
      throw runtimeExc;
    }));

    final Executable executable = t::orThrow;
    assertDoesNotThrow(executable);

    assertTrue(t.toString().startsWith("TryVoid"), t.toString());
  }

  @Test
  void testVoidFailure() {
    final UnsupportedOperationException runtimeExc = new UnsupportedOperationException();
    final IOException cause = new IOException();
    final TryVoid<Exception> t = TryVoid.failure(cause);
    assertEquals(t, TryVoid.run(() -> {
      throw cause;
    }));

    assertEquals(t, t.andGet(Try.success(1)::orThrow));
    assertEquals(t, t.andGet(Try.failure(cause)::orThrow));
    assertEquals(t, t.andGet(() -> 1));
    assertEquals(t, t.andGet(() -> {
      throw new URISyntaxException("", "");
    }));
    assertEquals(t, t.andGet(() -> {
      throw runtimeExc;
    }));

    assertEquals(t, t.andRun(TryVoid.success()::orThrow));
    assertEquals(t, t.andRun(TryVoid.failure(cause)::orThrow));
    assertEquals(t, t.andRun(() -> {
    }));
    assertEquals(t, t.andRun(() -> {
      throw cause;
    }));
    assertEquals(t, t.andRun(() -> {
      throw runtimeExc;
    }));

    final Throwing.Consumer<Exception, URISyntaxException> c =
        Mockito.mock(Throwing.Consumer.class);
    try {
      t.ifFailed(c);
      Mockito.verify(c).accept(cause);
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
    assertThrows(IOException.class, () -> t.ifFailed(e -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.ifFailed(e -> {
      throw runtimeExc;
    }));

    assertTrue(t.isFailure());
    assertFalse(t.isSuccess());

    assertEquals(3, t.map(() -> 2, e -> 3));
    assertThrows(IOException.class, () -> t.map(() -> 2, e -> {
      if (e.equals(cause)) {
        throw e;
      }
      throw runtimeExc;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.map(() -> 2, e -> {
      throw runtimeExc;
    }));

    assertEquals(TryVoid.success(), t.or(() -> {
    }));
    assertEquals(t, t.or(() -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.or(() -> {
      throw runtimeExc;
    }));

    assertThrows(IOException.class, t::orThrow);

    assertTrue(t.toString().startsWith("TryVoid"), t.toString());
  }

  @Test
  void testSafeSuccess() {
    final UnsupportedOperationException runtimeExc = new UnsupportedOperationException();
    final IOException cause = new IOException();
    final TryCatchAll<Integer> t = TryCatchAll.success(1);

    /*
     * TODO think abou TryCatchAllVoid#orThrows or how to obtain the cause in gnrl. Also, predicate
     * on cause?
     */

    assertEquals(t, TryCatchAll.get(() -> 1));
    assertNotEquals(t, TryCatchAllVoid.success());
    assertNotEquals(t, TryCatchAllVoid.failure(runtimeExc));
    assertNotEquals(t, TryCatchAllVoid.failure(cause));
    assertNotEquals(t, Try.success(1));
    assertNotEquals(t, Try.get(() -> 1));

    assertEquals(TryCatchAll.success(3), t.and(TryCatchAll.success(2), (i1, i2) -> i1 + i2));
    assertEquals(TryCatchAll.failure(cause),
        t.and(TryCatchAll.failure(cause), TryTests::mergeAdding));
    assertThrows(IOException.class, () -> t.and(TryCatchAll.success(2), TryTests::mergeThrowing));

    assertEquals(TryCatchAll.success(1), t.andConsume(i -> {
    }));
    assertEquals(TryCatchAll.failure(cause), t.andConsume(i -> {
      throw cause;
    }));
    assertEquals(TryCatchAll.failure(runtimeExc), t.andConsume(i -> {
      throw runtimeExc;
    }));

    assertEquals(TryCatchAll.success(1), t.andRun(TryCatchAllVoid.success()::orThrow));
    assertEquals(TryCatchAll.failure(cause), t.andRun(TryCatchAllVoid.failure(cause)::orThrow));

    assertEquals(TryCatchAll.success(1), t.andRun(() -> {
    }));
    assertEquals(TryCatchAll.failure(cause), t.andRun(() -> {
      throw cause;
    }));
    assertEquals(TryCatchAll.failure(runtimeExc), t.andRun(() -> {
      throw runtimeExc;
    }));

    assertEquals(TryCatchAll.success(5), t.andApply(i -> i + 4));
    assertEquals(TryCatchAll.failure(cause), t.andApply(i -> {
      throw cause;
    }));
    assertEquals(TryCatchAll.failure(runtimeExc), t.andApply(i -> {
      throw runtimeExc;
    }));

    assertFalse(t.isFailure());
    assertTrue(t.isSuccess());

    assertEquals(Optional.of(1), t.orConsumeCause(i -> {
    }));
    try {
      assertEquals(Optional.of(1), t.orConsumeCause(i -> {
        throw cause;
      }));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    assertEquals(Optional.of(1), t.orConsumeCause(i -> {
      throw runtimeExc;
    }));

    assertEquals(TryCatchAll.success(1), t.or(TryCatchAll.success(6)::orThrow, (e1, e2) -> cause));
    assertEquals(TryCatchAll.success(1),
        t.or(TryCatchAll.<Integer>failure(cause)::orThrow, (e1, e2) -> cause));

    assertEquals(TryCatchAll.success(1), t.or(() -> 6, (e1, e2) -> cause));
    assertEquals(TryCatchAll.success(1), t.or(() -> {
      throw cause;
    }, (e1, e2) -> cause));
    assertEquals(TryCatchAll.success(1), t.or(() -> {
      throw runtimeExc;
    }, (e1, e2) -> cause));

    assertEquals(1, t.orMapCause(e -> 8));
    try {
      assertEquals(1, t.orMapCause(e -> {
        throw cause;
      }));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    assertEquals(1, t.orMapCause(e -> {
      throw runtimeExc;
    }));

    try {
      assertEquals(1, t.orThrow());
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }

    assertTrue(t.toString().startsWith("TryCatchAll"), t.toString());
  }

  @Test
  void testSafeFailure() {
    final UnsupportedOperationException runtimeExc = new UnsupportedOperationException();
    final IOException cause = new IOException();
    final TryCatchAll<Integer> t = TryCatchAll.failure(runtimeExc);

    /*
     * TODO think abou TryCatchAllVoid#orThrows or how to obtain the cause in gnrl. Also, predicate
     * on cause?
     */

    assertNotEquals(t, TryCatchAll.get(() -> 1));
    assertEquals(t, TryCatchAll.get(() -> {
      throw runtimeExc;
    }));
    assertNotEquals(t, Try.get(() -> 1));
    assertNotEquals(t, Try.success(1));
    assertNotEquals(t, TryVoid.failure(runtimeExc));
    assertNotEquals(t, TryCatchAllVoid.success());
    assertEquals(t, TryCatchAllVoid.failure(runtimeExc));

    assertEquals(t, t.and(TryCatchAll.success(2), (i1, i2) -> i1 + i2));
    assertEquals(t, t.and(TryCatchAll.failure(cause), TryTests::mergeAdding));
    try {
      assertEquals(t, t.and(TryCatchAll.success(2), TryTests::mergeThrowing));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    assertEquals(t, t.andConsume(i -> {
    }));
    assertEquals(t, t.andConsume(i -> {
      throw cause;
    }));
    assertEquals(t, t.andConsume(i -> {
      throw runtimeExc;
    }));

    assertEquals(t, t.andRun(TryCatchAllVoid.success()::orThrow));
    assertEquals(t, t.andRun(TryCatchAllVoid.failure(cause)::orThrow));

    assertEquals(t, t.andRun(() -> {
    }));
    assertEquals(t, t.andRun(() -> {
      throw cause;
    }));
    assertEquals(t, t.andRun(() -> {
      throw runtimeExc;
    }));

    assertEquals(t, t.andApply(i -> i + 4));
    assertEquals(t, t.andApply(i -> {
      throw cause;
    }));
    assertEquals(t, t.andApply(i -> {
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

    assertEquals(TryCatchAll.success(6), t.or(() -> 6, (e1, e2) -> cause));
    assertEquals(TryCatchAll.failure(cause), t.or(() -> {
      throw cause;
    }, (e1, e2) -> e1.equals(runtimeExc) && e2.equals(cause) ? cause : runtimeExc));
    assertEquals(TryCatchAll.failure(runtimeExc), t.or(() -> {
      throw runtimeExc;
    }, (e1, e2) -> e1.equals(runtimeExc) && e2.equals(runtimeExc) ? runtimeExc : cause));
    assertEquals(TryCatchAll.success(6), t.or(TryCatchAll.success(6)::orThrow, (e1, e2) -> cause));
    assertEquals(TryCatchAll.failure(cause), t.or(TryCatchAll.<Integer>failure(cause)::orThrow,
        (e1, e2) -> e1.equals(runtimeExc) && e2.equals(cause) ? cause : runtimeExc));

    assertEquals(8, t.orMapCause(e -> 8));
    assertThrows(IOException.class, () -> t.orMapCause(e -> {
      throw cause;
    }));
    assertThrows(UnsupportedOperationException.class, () -> t.orMapCause(e -> {
      throw runtimeExc;
    }));

    assertThrows(UnsupportedOperationException.class, t::orThrow);

    assertTrue(t.toString().startsWith("TryCatchAll"), t.toString());
  }

  static int mergeAdding(int i1, int i2) {
    return i1 + i2;
  }

  @SuppressWarnings("unused")
  static int mergeThrowing(int i1, int i2) throws IOException {
    throw new IOException();
  }
}

