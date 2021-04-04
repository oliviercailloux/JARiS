package io.github.oliviercailloux.jaris.exceptions;

import java.util.Optional;

/**
 * A sort of try optional such that a success has an associated value. Suitable for Try and TrySafe,
 * depending on the catching strategy. The name indicates that this interface applies to both
 * catching strategies.
 *
 * @param <T> the type of result kept in this object if it is a success.
 * @param <X> the type of cause kept in this object if it is a failure.
 */
interface TryVariableCatchInterface<T, X extends Throwable> {

  /**
   * Returns <code>true</code> iff this object contains a result (and not a cause).
   *
   * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
   */
  public boolean isSuccess();

  /**
   * Return <code>true</code> iff this object contains a cause (and not a result).
   *
   * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
   */
  public boolean isFailure();

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
  public <U, Y extends Exception> U map(
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
  public <Y extends Exception> T orMapCause(
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
  public <Y extends Exception> Optional<T> orConsumeCause(Throwing.Consumer<? super X, Y> consumer)
      throws Y;

  /**
   * Returns the result contained in this instance if this instance is a success, or throws the
   * cause contained in this instance.
   *
   * @return the result that this success contains
   * @throws X iff this instance is a failure
   */
  public T orThrow() throws X;

  /**
   * Attempts to get a result from the given supplier iff this instance is a failure, and return the
   * resulting try if the supplier succeeds and the merged cause of this instance and of the failed
   * supplier if it threw a caught throwable; otherwise, returns this instance.
   *
   * @param <Y> a type of exception that the provided supplier may throw
   * @param <Z> the type of cause that the returned try will be declared to contain
   * @param <W> a type of exception that the provided merger may throw
   * @param supplier the supplier that is invoked if this try is a failure
   * @param exceptionsMerger the function invoked to merge both exceptions if this try is a failure
   *        and the given supplier threw a checked exception
   * @return a success if this instance is a success or the given supplier returned a result
   * @throws W iff the merger was applied and threw a checked exception
   */
  public <Y extends Throwable, Z extends Throwable, W extends Exception> TryVariableCatchInterface<T, ?> or(
      Throwing.Supplier<? extends T, Y> supplier,
      Throwing.BiFunction<? super X, ? super Y, ? extends Z, W> exceptionsMerger) throws W;

  /**
   * Runs the runnable iff this instance is a success, and returns the result; otherwise, returns
   * this instance.
   *
   * @param runnable the functional interface to run if this instance is a success
   * @return a success iff this instance is a success and the provided runnable terminated without
   *         throwing
   */
  public TryVariableCatchInterface<T, ?> andRun(Throwing.Runnable<? extends X> runnable);

  /**
   * Runs the consumer iff this instance is a success, and returns this instance if it succeeds and
   * the cause of failure if it throws a caught throwable; otherwise, returns this instance.
   *
   * @param consumer the functional interface to run if this instance is a success
   * @return a success iff this instance is a success and the provided consumer terminated without
   *         throwing
   */
  public TryVariableCatchInterface<T, ?> andConsume(
      Throwing.Consumer<? super T, ? extends X> consumer);

  /**
   * Consider the given try iff this instance is a success, and returns the result contained in this
   * instance merged to the provided result if they both are success, or the provided failure if it
   * is a failure; otherwise, returns this failure.
   *
   * @param <U> the type of result that the provided try is declared to contain
   * @param <V> the type of result that the returned try will be declared to contain
   * @param <Y> a type of exception that the provided merger may throw
   * @param t2 the try to consider if this try is a success
   * @param merger the function invoked to merge the results if both this and the given try are
   *        successes
   * @return a success if this instance and the given try are two successes
   * @throws Y iff the merger was applied and threw a checked exception
   */
  public <U, V, Y extends Exception> TryVariableCatchInterface<V, X> and(
      TryVariableCatchInterface<U, ? extends X> t2,
      Throwing.BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y;

  /**
   * Applies the given mapper iff this instance is a success, and returns the transformed success if
   * it succeeds, or the cause of the failure if it throws a catchable throwable; otherwise, returns
   * this instance.
   *
   * @param <U> the type of result that the returned try will be declared to contain
   * @param mapper the mapper to apply to the result contained in this instance if it is a success
   * @return a success iff this instance is a success and the provided mapper does not throw
   */
  public <U> TryVariableCatchInterface<U, X> flatMap(
      Throwing.Function<? super T, ? extends U, ? extends X> mapper);

}
