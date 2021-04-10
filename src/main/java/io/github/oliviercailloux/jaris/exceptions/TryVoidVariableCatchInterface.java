package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;
import io.github.oliviercailloux.jaris.exceptions.old.Try;
import java.util.function.Function;

/**
 * A sort of try optional such that a success has no associated value. Suitable for TryVoid and
 * TryVoidCatchAll, depending on the catching strategy. The name indicates that this interface
 * applies to both catching strategies.
 *
 * @param <X> the type of cause kept in this object if it is a failure.
 */
interface TryVoidVariableCatchInterface<X extends Throwable, Z extends Throwable> {

  /**
   * Returns <code>true</code> iff this instance is a success, hence, contains no cause.
   *
   * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
   */
  public boolean isSuccess();

  /**
   * Return <code>true</code> iff this object contains a cause.
   *
   * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
   */
  public boolean isFailure();

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
  public <T, Y extends Exception> T map(Throwing.Supplier<? extends T, ? extends Y> supplier,
      Throwing.Function<? super X, ? extends T, ? extends Y> causeTransformation) throws Y;

  /**
   * If this instance is a failure, invokes the given consumer using the cause contained in this
   * instance. If this instance is a success, do nothing.
   *
   * @param <Y> a type of exception that the provided consumer may throw
   * @param consumer the consumer to invoke if this instance is a failure
   * @throws Y iff the consumer was invoked and threw a checked exception
   */
  public <Y extends Exception> void ifFailed(Throwing.Consumer<? super X, Y> consumer) throws Y;

  /**
   * If this instance is a failure, throws the cause it contains. Otherwise, do nothing.
   *
   * @throws X iff this instance contains a cause
   */
  public <Y extends Z> void orThrow(Function<X, Y> causeTransformation) throws Y;

  /**
   * If this instance is a success, returns a try representing the result of invoking the given
   * supplier; otherwise, returns this failure.
   * <p>
   * If this instance is a failure, it is returned, without invoking the given supplier. Otherwise,
   * the given supplier is invoked. If it terminates without throwing, a success is returned,
   * containing the result just supplied by the supplier. If the supplier throws a checked
   * exception, a failure is returned, containing the cause it threw.
   *
   * @param <T> the type of result that the returned try will be declared to contain
   * @param <X> the type of cause that the returned try will be declared to contain
   * @param supplier the supplier to attempt to get a result from if this instance is a success.
   * @return a success iff this instance is a success and the given supplier terminated without
   *         throwing.
   * @see Try#get(Supplier)
   */
  public <T> TryVariableCatchInterface<T, X, Z> andGet(
      Throwing.Supplier<? extends T, ? extends X> supplier);

  /**
   * If this instance is a success, returns a {@code TryVoid} instance representing the result of
   * invoking the given runnable; otherwise, returns this failure.
   * <p>
   * If this instance is a failure, it is returned, without invoking the given runnable. Otherwise,
   * the given runnable is invoked. If it terminates without throwing, a success is returned. If the
   * runnable throws a checked exception, a failure is returned, containing the cause it threw.
   *
   * @param <X> the type of cause that the returned instance will be declared to contain
   * @param runnable the runnable to attempt to run if this instance is a success.
   * @return a success iff this instance is a success and the given runnable terminated without
   *         throwing.
   * @see #run(Runnable)
   */
  public TryVoidVariableCatchInterface<X, Z> andRun(Throwing.Runnable<? extends X> runnable);

  /**
   * If this instance is a failure, returns a {@code TryVoid} instance representing the result of
   * invoking the given runnable; otherwise, returns this success.
   * <p>
   * If this instance is a success, it is returned, without invoking the given runnable. Otherwise,
   * the given runnable is invoked. If it terminates without throwing, a success is returned. If the
   * runnable throws a checked exception, a failure is returned, containing the cause it threw.
   *
   * @param <X> the type of cause that the returned instance will be declared to contain
   * @param runnable the runnable to attempt to run if this instance is a failure.
   * @return a success iff this instance is a success or the given runnable terminated without
   *         throwing.
   * @see #run(Runnable)
   */
  public TryVoidVariableCatchInterface<X, Z> or(Throwing.Runnable<? extends X> runnable);

  /**
   * Returns a string representation of this object, suitable for debug.
   */
  @Override
  public String toString();

}
