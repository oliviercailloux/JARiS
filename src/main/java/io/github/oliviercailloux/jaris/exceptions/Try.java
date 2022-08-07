package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.throwing.TBiFunction;
import io.github.oliviercailloux.jaris.throwing.TConsumer;
import io.github.oliviercailloux.jaris.throwing.TFunction;
import io.github.oliviercailloux.jaris.throwing.TRunnable;
import io.github.oliviercailloux.jaris.throwing.TSupplier;

/**
 * Represents either a result or a failure and provides operations to deal with cases of successes
 * and of failures in a unified way.
 * <p>
 * An instance of this class contains either a (non-{@code null}) result, in which case it is called
 * a “success”; or a cause of type {@code X} (some {@link Exception}), in which case it is called a
 * “failure”.
 * </p>
 * <p>
 * Instances of this type are immutable.
 * </p>
 * <p>
 * This type provides transformation operations that admit functional operators that can throw
 * exceptions. Some of these methods will catch checked exceptions thrown by such functional
 * operators, while others will propagate any exception thrown to the caller (see the method
 * documentation). When the documentation of a method indicates that it catches checked exceptions
 * thrown by some provided functional interface, it is implicit that if the provided functional
 * interface throws anything that is not a checked exception, then it is not caught, and simply
 * propagated to the caller.
 * </p>
 * <p>
 * Inspired by <a href="https://github.com/vavr-io/vavr">Vavr</a>. One notable difference is that
 * this library does not sneaky throw (see the contract of Vavr’s <code>Try#<a href=
 * "https://github.com/vavr-io/vavr/blob/9a40af5cec2622a8ce068d5833a2bf07671f5eed/src/main/java/io/vavr/control/Try.java#L629">get()</a></code>
 * and its <a href=
 * "https://github.com/vavr-io/vavr/blob/9a40af5cec2622a8ce068d5833a2bf07671f5eed/src/main/java/io/vavr/control/Try.java#L1305">implementation</a>).
 * </p>
 *
 * @param <T> the type of result possibly kept in the instance.
 * @param <X> the type of cause possibly kept in the instance.
 */
public interface Try<T, X extends Exception>
    extends TryOptionalImpl.TryVariableCatchInterface<T, X, Exception> {
  /**
   * Returns a success containing the given result.
   *
   * @param <T> the type of result declared to be (and effectively) kept in the instance
   * @param <X> the type of cause declared to be (but not effectively) kept in the instance.
   * @param result the result to contain
   * @return a success
   */
  public static <T, X extends Exception> Try<T, X> success(T result) {
    return TryOptionalImpl.TrySuccess.given(result);
  }

  /**
   * Returns a failure containing the given cause.
   *
   * @param <T> the type of result declared to be (but not effectively) kept in the instance
   * @param <X> the type of cause declared to be (and effectively) kept in the instance.
   * @param cause the cause to contain
   * @return a failure
   */
  public static <T, X extends Exception> Try<T, X> failure(X cause) {
    return TryOptionalImpl.TryFailure.given(cause);
  }

  /**
   * Attempts to get and encapsulate a result from the given supplier.
   * <p>
   * This method returns a failure iff the given supplier throws a checked exception.
   *
   * @param <T> the type of result declared to be kept in the instance
   * @param <X> the type of cause declared to be kept in the instance; a sort of exception that the
   *        supplier may throw.
   * @param supplier the supplier to get a result from
   * @return a success containing the result if the supplier returns a result; a failure containing
   *         the throwable if the supplier throws a checked exception
   * @throws NullPointerException if the supplier returns {@code null}
   */
  public static <T, X extends Exception> Try<T, X>
      get(TSupplier<? extends T, ? extends X> supplier) {
    try {
      return success(supplier.get());
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      /* This is safe, provided the supplier did not sneaky-throw. */
      @SuppressWarnings("unchecked")
      final X exc = (X) e;
      return failure(exc);
    }
  }

  /**
   * Runs the runnable iff this instance is a success, and returns this instance if it succeeds and
   * the cause of failure if it throws a checked exception; otherwise, returns this instance.
   * <p>
   * If this instance is a failure, returns this instance without running the provided runnable.
   * Otherwise, if the runnable succeeds (that is, does not throw), returns this instance.
   * Otherwise, if the runnable throws a checked exception, returns a failure containing the cause
   * it threw.
   *
   * @param runnable the function to run if this instance is a success
   * @return a success iff this instance is a success and the provided runnable terminated without
   *         throwing
   */
  @Override
  public abstract Try<T, X> andRun(TRunnable<? extends X> runnable);

  /**
   * Runs the consumer iff this instance is a success, and returns this instance if it succeeds and
   * the cause of failure if it throws a checked exception; otherwise, returns this instance.
   * <p>
   * If this instance is a failure, returns this instance without running the provided consumer.
   * Otherwise, if the consumer succeeds (that is, does not throw), returns this instance.
   * Otherwise, if the consumer throws a checked exception, returns a failure containing the cause
   * it threw.
   *
   * @param consumer the function to run if this instance is a success
   * @return a success iff this instance is a success and the provided consumer terminated without
   *         throwing
   */
  @Override
  public abstract Try<T, X> andConsume(TConsumer<? super T, ? extends X> consumer);

  /**
   * Returns this failure if this instance is a failure; the provided failure if the provided try is
   * a failure and this instance is a success; and a success containing the merge of the result
   * contained in this instance and the one contained in {@code t2}, if they both are successes and
   * {@code merger} does not return {@code null}.
   *
   * @param <U> the type of result that the provided try is declared to contain
   * @param <V> the type of result that the returned try will be declared to contain
   * @param <Y> a type of exception that the provided merger may throw
   * @param t2 the try to consider if this try is a success
   * @param merger the function invoked to merge the results if both this and the given try are
   *        successes
   * @return a success if this instance and the given try are two successes
   * @throws Y if the merger was applied and threw an exception of type {@code Y}
   * @throws NullPointerException if the merger was applied and returned {@code null}
   */
  public abstract <U, V, Y extends Exception> Try<V, X> and(Try<U, ? extends X> t2,
      TBiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y;

  /**
   * Returns this failure if this instance is a failure; a failure containing the cause thrown by
   * the given function if it threw a checked exception; or a success containing the result of
   * applying the provided mapper to the result contained in this instance if it is a success and
   * the mapper returned a non-{@code null} result.
   * <p>
   * Equivalent to {@code t.map(r -> Try.get(() -> mapper.apply(r)), c -> t)}.
   *
   * @param <U> the type of result that the returned try will be declared to contain
   * @param mapper the mapper to apply to the result contained in this instance if it is a success
   * @return a success iff this instance is a success and the provided mapper returns a
   *         non-{@code null} value
   * @throws NullPointerException if the mapper was applied and returned {@code null}
   */
  @Override
  public abstract <U> Try<U, X>
      andApply(TFunction<? super T, ? extends U, ? extends X> mapper);

  /**
   * Returns this instance if it is a success; otherwise, returns a success if the supplier returns
   * a non-{@code null} result.
   * <p>
   * Returns this instance if it is a success. Otherwise, attempts to get a result from the given
   * supplier. If this succeeds, that is, if the supplier returns a non-{@code null} result, returns
   * a success containing that result. Otherwise, if the supplier throws a checked exception, this
   * method merges both exceptions using the given {@code exceptionMerger} and returns a failure
   * containing that merged cause.
   *
   * @param <Y> a type of exception that the provided supplier may throw
   * @param <Z> the type of cause that the returned try will be declared to contain
   * @param <W> a type of exception that the provided merger may throw
   * @param supplier the supplier to invoke iff this try is a failure
   * @param exceptionsMerger the function invoked to merge both exceptions iff this try is a failure
   *        and the given supplier threw a checked exception
   * @return a success if this instance is a success or the given supplier returned a result
   * @throws W if the merger was applied and threw an exception of type {@code W}
   * @throws NullPointerException if the supplier or the merger was applied and returned
   *         {@code null}
   */
  public abstract <Y extends Exception, Z extends Exception, W extends Exception> Try<T, Z> or(
      TSupplier<? extends T, Y> supplier,
      TBiFunction<? super X, ? super Y, ? extends Z, W> exceptionsMerger) throws W;
}
