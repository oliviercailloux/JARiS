package io.github.oliviercailloux.jaris.exceptions.old;

import io.github.oliviercailloux.jaris.exceptions.Throwing;
import java.util.Optional;

/**
 * An internal try type for implementation by Try and TrySafe.
 *
 * Contains the terminal methods, which do not depend on whether all throwables or only checked
 * exceptions are caught when producing TryOptional instances.
 *
 */
interface TryGeneral<T, X extends Throwable> {

  /**
   * Returns the transformed result contained in this instance if it is a success, using the
   * provided {@code transformation}; or the transformed cause contained in this instance if it is a
   * failure, using the provided {@code causeTransformation}.
   * <p>
   * This method necessarily applies exactly one of the provided functions.
   *
   * @param <U> the type of transformed result to return
   * @param <Y> a type of exception that the provided functions may throw
   * @param transformation a function to apply to the result if this instance is a success
   * @param causeTransformation a function to apply to the cause if this instance is a failure
   * @return the transformed result or cause
   * @throws Y iff the function that was applied threw a checked exception
   */
  public abstract <U, Y extends Exception> U map(
      Throwing.Function<? super T, ? extends U, ? extends Y> transformation,
      Throwing.Function<? super X, ? extends U, ? extends Y> causeTransformation) throws Y;

  /**
   * Returns the result contained in this instance if it is a success, without applying the provided
   * function; or returns the transformed cause contained in this instance if it is a failure, using
   * the provided {@code causeTransformation}.
   * <p>
   * Equivalent to: {@code map(Function#identity(), causeTransformation)}.
   *
   * @param <Y> a type of exception that the provided function may throw
   * @param causeTransformation the function to apply if this instance is a failure
   * @return the result, or the transformed cause
   * @throws Y iff the function was applied and threw a checked exception
   */
  public abstract <Y extends Exception> T orMapCause(
      Throwing.Function<? super X, ? extends T, Y> causeTransformation) throws Y;

  /**
   * Returns an optional containing the result of this instance, without invoking the given
   * consumer, if this try is a success; otherwise, invokes the given consumer and returns an empty
   * optional.
   *
   * @param <Y> a type of exception that the provided consumer may throw
   * @param consumer the consumer to invoke if this instance is a failure
   * @return an optional, containing the result if this instance is a success, empty otherwise
   * @throws Y iff the consumer was invoked and threw a checked exception
   */
  public abstract <Y extends Exception> Optional<T> orConsumeCause(
      Throwing.Consumer<? super X, Y> consumer) throws Y;

  /**
   * Returns the result contained in this instance if this instance is a success, or throws the
   * cause contained in this instance.
   *
   * @return the result that this success contains
   * @throws X iff this instance is a failure
   */
  public abstract T orThrow() throws X;

}
