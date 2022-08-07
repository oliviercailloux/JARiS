package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.throwing.TRunnable;
import io.github.oliviercailloux.jaris.throwing.TSupplier;

/**
 * A variant of {@link Try} that contains no result in case of success.
 * <p>
 * An instance of this class is either a <em>success</em> or a <em>failure</em>. In the latter case,
 * it contains a <em>cause</em> (some {@link Exception}) of type {@code X}.
 * </p>
 * <p>
 * Instances of this type are immutable.
 * </p>
 *
 * @param <X> the type of cause possibly kept in the instance.
 */
public interface TryVoid<X extends Exception>
    extends TryOptionalImpl.TryVariableCatchVoidInterface<X, Exception> {
  /**
   * Returns a success.
   *
   * @param <X> the type of cause declared to be (but not effectively) kept in the instance.
   * @return a success
   */
  public static <X extends Exception> TryVoid<X> success() {
    return TryOptionalImpl.TryVoidSuccess.given();
  }

  /**
   * Returns a failure containing the given cause.
   *
   * @param <X> the type of cause declared to be (and effectively) kept in the instance.
   * @param cause the cause to contain
   * @return a failure
   */
  public static <X extends Exception> TryVoid<X> failure(X cause) {
    return TryOptionalImpl.TryVoidFailure.given(cause);
  }

  /**
   * Attempts to run the given runnable, and returns a success if it succeeds or a failure
   * containing the checked exception thrown by the runnable if it threw one; otherwise, rethrows
   * the non-checked throwable that the runnable threw.
   *
   * @param <X> the type of cause declared to be kept in the returned instance
   * @param runnable the instance to run
   * @return a success iff the given runnable did not throw.
   */
  public static <X extends Exception> TryVoid<X> run(TRunnable<? extends X> runnable) {
    try {
      runnable.run();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      /* This is safe, provided the runnable did not sneaky-throw. */
      @SuppressWarnings("unchecked")
      final X exc = (X) e;
      return failure(exc);
    }
    return success();
  }

  /**
   * If this instance is a success, returns a try representing the result of invoking the given
   * supplier; otherwise, returns this failure as a try.
   * <p>
   * If this instance is a failure, a try containing the cause is returned, without invoking the
   * given supplier. Otherwise, the given supplier is invoked. If it supplies a non-{@code null}
   * result, a success is returned, containing that result. If the supplier throws a checked
   * exception, a failure is returned, containing the cause it threw.
   *
   * @param <T> the type of result that the returned try will be declared to contain
   * @param supplier the supplier to attempt to get a result from if this instance is a success.
   * @return a success iff this instance is a success and the given supplier terminated without
   *         throwing.
   * @throws NullPointerException if the supplier returns {@code null}
   * @see Try#get(Throwing.TSupplier)
   */
  @Override
  public <T> Try<T, X> andGet(TSupplier<? extends T, ? extends X> supplier);

  /**
   * If this instance is a success, returns a {@code TryVoid} instance representing the result of
   * invoking the given runnable; otherwise, returns this failure.
   * <p>
   * If this instance is a failure, it is returned, without invoking the given runnable. Otherwise,
   * the given runnable is invoked. If it terminates without throwing, a success is returned. If the
   * runnable throws a checked exception, a failure is returned, containing the cause it threw.
   *
   * @param runnable the runnable to attempt to run if this instance is a success.
   * @return a success iff this instance is a success and the given runnable terminated without
   *         throwing.
   * @see #run(Throwing.TRunnable)
   */
  @Override
  public TryVoid<X> andRun(TRunnable<? extends X> runnable);

  /**
   * If this instance is a failure, returns a {@code TryVoid} instance representing the result of
   * invoking the given runnable; otherwise, returns this success.
   * <p>
   * If this instance is a success, it is returned, without invoking the given runnable. Otherwise,
   * the given runnable is invoked. If it terminates without throwing, a success is returned. If the
   * runnable throws a checked exception, a failure is returned, containing the cause it threw.
   *
   * @param runnable the runnable to attempt to run if this instance is a failure.
   * @return a success iff this instance is a success or the given runnable terminated without
   *         throwing.
   * @see #run(Throwing.TRunnable)
   */
  @Override
  public TryVoid<X> or(TRunnable<? extends X> runnable);
}
