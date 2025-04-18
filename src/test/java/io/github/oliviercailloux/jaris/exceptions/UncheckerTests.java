package io.github.oliviercailloux.jaris.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.oliviercailloux.jaris.throwing.TSupplier;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UncheckerTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(UncheckerTests.class);

  private static void runnableThrowing() throws SQLException {
    throw new SQLException();
  }

  @SuppressWarnings("unused")
  private static void runnableNotThrowing() throws SQLException {
    /* Do nothing. */
  }

  private static int supplierThrowing() throws SQLException {
    throw new SQLException();
  }

  @SuppressWarnings("unused")
  private static int supplierNotThrowing() throws SQLException {
    return 4;
  }

  @Test
  void testRunnableThrows() {
    final Unchecker<SQLException, IllegalStateException> unchecker =
        Unchecker.wrappingWith(IllegalStateException::new);
    final IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> unchecker.call(UncheckerTests::runnableThrowing));
    assertTrue(e.getCause() instanceof SQLException);
  }

  @Test
  void testRunnableDoesNotThrow() {
    final Unchecker<SQLException, IllegalStateException> unchecker =
        Unchecker.wrappingWith(IllegalStateException::new);
    unchecker.call(UncheckerTests::runnableNotThrowing);
  }

  @Test
  void testSupplierDoesNotThrow() {
    final Unchecker<SQLException, IllegalStateException> unchecker =
        Unchecker.wrappingWith(IllegalStateException::new);
    TSupplier<Integer, SQLException> supplier = UncheckerTests::supplierNotThrowing;
    assertEquals(4, unchecker.getUsing(supplier));
  }

  @Test
  void testToSupplier() {
    final Unchecker<SQLException, IllegalStateException> unchecker =
        Unchecker.wrappingWith(IllegalStateException::new);
    final Supplier<Integer> uncheckedSupplier =
        unchecker.wrapSupplier(UncheckerTests::supplierThrowing);
    assertThrows(IllegalStateException.class, uncheckedSupplier::get);
  }
}
