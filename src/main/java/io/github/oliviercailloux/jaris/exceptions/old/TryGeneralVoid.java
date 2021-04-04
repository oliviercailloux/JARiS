package io.github.oliviercailloux.jaris.exceptions.old;

import io.github.oliviercailloux.jaris.exceptions.Throwing;

/**
 * An internal try type for implementation by {@link TryVoid} and {@link TrySafeVoid}.
 *
 * Contains the terminal methods, which do not depend on whether all throwables or only checked
 * exceptions are caught when producing {@link TryOptional} instances.
 *
 */
interface TryGeneralVoid<X extends Throwable> {

  /**
   * Returns the supplied result if this instance is a success, using the provided {@code supplier};
   * or the transformed cause contained in this instance if it is a failure, using the provided
   * {@code causeTransformation}.
   * <p>
   * This method necessarily invokes exactly one of the provided functional interfaces.
   *
   * @param <T> the type of (supplied or transformed) result to return
   * @param <Y> a type of exception that the provided functions may throw
   * @param supplier a supplier to get a result from if this instance is a success
   * @param causeTransformation a function to apply to the cause if this instance is a failure
   * @return the supplied result or transformed cause
   * @throws Y iff the functional interface that was invoked threw a checked exception
   */
  public abstract <T, Y extends Exception> T map(
      Throwing.Supplier<? extends T, ? extends Y> supplier,
      Throwing.Function<? super X, ? extends T, ? extends Y> causeTransformation) throws Y;

  /**
   * If this instance is a failure, invokes the given consumer using the cause contained in this
   * instance. If this instance is a success, do nothing.
   *
   * @param <Y> a type of exception that the provided consumer may throw
   * @param consumer the consumer to invoke if this instance is a failure
   * @throws Y iff the consumer was invoked and threw a checked exception
   */
  public abstract <Y extends Exception> void ifFailed(Throwing.Consumer<? super X, Y> consumer)
      throws Y;

  /**
   * If this instance is a failure, throws the cause it contains. Otherwise, do nothing.
   *
   * @throws X iff this instance contains a cause
   */
  public abstract void orThrow() throws X;

}
