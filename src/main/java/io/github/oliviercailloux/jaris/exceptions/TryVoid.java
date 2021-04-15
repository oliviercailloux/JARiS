package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.impl.TryOptional;


/**
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
    extends TryOptional.TryVoidVariableCatchInterface<X, Exception> {
  /**
   * Returns a success.
   *
   * @param <X> the type of cause declared to be (but not effectively) kept in the instance.
   */
  public static <X extends Exception> TryVoid<X> success() {
    return TryOptional.TryVoidSuccess.given();
  }

  /**
   * Returns a failure containing the given cause.
   *
   * @param <X> the type of cause declared to be (and effectively) kept in the instance.
   * @param cause the cause to contain
   */
  public static <X extends Exception> TryVoid<X> failure(X cause) {
    return TryOptional.TryVoidFailure.given(cause);
  }

  /**
   * Attempts to run the given runnable, and returns a success if it succeeds or a failure
   * containing the checked exception thrown by the runnable if it threw one; otherwise, rethrows
   * the non-checked throwable that the runnable threw.
   *
   * @return a success iff the given runnable did not throw.
   */
  public static <X extends Exception> TryVoid<X> run(Throwing.Runnable<? extends X> runnable) {
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
   * given supplier. Otherwise, the given supplier is invoked. If it terminates without throwing, a
   * success is returned, containing the result just supplied by the supplier. If the supplier
   * throws a checked exception, a failure is returned, containing the cause it threw.
   *
   * @param <T> the type of result that the returned try will be declared to contain
   * @param supplier the supplier to attempt to get a result from if this instance is a success.
   * @return a success iff this instance is a success and the given supplier terminated without
   *         throwing.
   * @see Try#get(Throwing.Supplier)
   */
  @Override
  public <T> Try<T, X> andGet(Throwing.Supplier<? extends T, ? extends X> supplier);

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
   * @see #run(Throwing.Runnable)
   */
  @Override
  public TryVoid<X> andRun(Throwing.Runnable<? extends X> runnable);

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
   * @see #run(Throwing.Runnable)
   */
  @Override
  public TryVoid<X> or(Throwing.Runnable<? extends X> runnable);
}
