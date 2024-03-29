package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.throwing.TRunnable;
import io.github.oliviercailloux.jaris.throwing.TSupplier;

/**
 * A variant of {@link TryCatchAll} that contains no result in case of success.
 * <p>
 * An instance of this class is either a <em>success</em> or a <em>failure</em>. In the latter case,
 * it contains a <em>cause</em> (some {@link Exception}) of type {@code X}.
 * </p>
 * <p>
 * Instances of this type are immutable.
 * </p>
 */
public interface TryCatchAllVoid
    extends TryOptionalImpl.TryVariableCatchVoidInterface<Throwable, Throwable> {
  /**
   * Returns a success.
   *
   * @return a success
   */
  public static TryCatchAllVoid success() {
    return TryOptionalImpl.TryCatchAllVoidSuccess.given();
  }

  /**
   * Returns a failure containing the given cause.
   *
   * @param cause the cause to contain
   * @return a success
   */
  public static TryCatchAllVoid failure(Throwable cause) {
    return TryOptionalImpl.TryCatchAllVoidFailure.given(cause);
  }

  /**
   * Attempts to run the given runnable, and returns a success if it succeeds or a failure
   * containing the throwable thrown by the runnable if it throws.
   *
   * @param runnable the runnable to attempt to run
   * @return a success iff the given runnable did not throw.
   */
  public static TryCatchAllVoid run(TRunnable<?> runnable) {
    try {
      runnable.run();
    } catch (Throwable e) {
      return failure(e);
    }
    return success();
  }

  /**
   * If this instance is a success, returns a try representing the result of invoking the given
   * supplier; otherwise, returns this failure.
   * <p>
   * If this instance is a failure, it is returned, without invoking the given supplier. Otherwise,
   * the given supplier is invoked. If it supplies a non {@code null} result, a success is returned,
   * containing that result. If the supplier throws a checked exception (or returns {@code null}), a
   * failure is returned, containing the cause it threw (or a {@link NullPointerException},
   * respectively).
   * </p>
   *
   * @param <T> the type of result that the returned try will be declared to contain
   * @param supplier the supplier to attempt to get a result from if this instance is a success.
   * @return a success iff this instance is a success and the given supplier terminated without
   *         throwing.
   * @see Try#get(TSupplier)
   */
  @Override
  public <T> TryCatchAll<T> andGet(TSupplier<? extends T, ? extends Throwable> supplier);

  /**
   * If this instance is a success, returns a {@code TryVoid} instance representing the result of
   * invoking the given runnable; otherwise, returns this failure.
   * <p>
   * If this instance is a failure, it is returned, without invoking the given runnable. Otherwise,
   * the given runnable is invoked. If it terminates without throwing, a success is returned. If the
   * runnable throws a checked exception, a failure is returned, containing the cause it threw.
   * </p>
   *
   * @param runnable the runnable to attempt to run if this instance is a success.
   * @return a success iff this instance is a success and the given runnable terminated without
   *         throwing.
   * @see #run(TRunnable)
   */
  @Override
  public TryCatchAllVoid andRun(TRunnable<?> runnable);

  /**
   * If this instance is a failure, returns a {@code TryVoid} instance representing the result of
   * invoking the given runnable; otherwise, returns this success.
   * <p>
   * If this instance is a success, it is returned, without invoking the given runnable. Otherwise,
   * the given runnable is invoked. If it terminates without throwing, a success is returned. If the
   * runnable throws a checked exception, a failure is returned, containing the cause it threw.
   * </p>
   *
   * @param runnable the runnable to attempt to run if this instance is a failure.
   * @return a success iff this instance is a success or the given runnable terminated without
   *         throwing.
   * @see #run(TRunnable)
   */
  @Override
  public TryCatchAllVoid or(TRunnable<?> runnable);
}
