package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;

/**
 * A variant of {@link TryCatchAll} that contains no result in case of success.
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
public interface TryCatchAllVoid
    extends TryOptional.TryVariableCatchVoidInterface<Throwable, Throwable> {
  /**
   * Returns a success.
   *
   * @param <X> the type of cause declared to be (but not effectively) kept in the instance.
   */
  public static TryCatchAllVoid success() {
    return TryOptional.TryCatchAllVoidSuccess.given();
  }

  /**
   * Returns a failure containing the given cause.
   *
   * @param <X> the type of cause declared to be (and effectively) kept in the instance.
   * @param cause the cause to contain
   */
  public static TryCatchAllVoid failure(Throwable cause) {
    return TryOptional.TryCatchAllVoidFailure.given(cause);
  }

  /**
   * Attempts to run the given runnable, and returns a success if it succeeds or a failure
   * containing the throwable thrown by the runnable if it throws.
   *
   * @return a success iff the given runnable did not throw.
   */
  public static TryCatchAllVoid run(Throwing.Runnable<?> runnable) {
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
   * the given supplier is invoked. If it terminates without throwing, a success is returned,
   * containing the result just supplied by the supplier. If the supplier throws a checked
   * exception, a failure is returned, containing the cause it threw.
   *
   * @param <T> the type of result that the returned try will be declared to contain
   * @param supplier the supplier to attempt to get a result from if this instance is a success.
   * @return a success iff this instance is a success and the given supplier terminated without
   *         throwing.
   * @see Try#get(Supplier)
   */
  @Override
  public <T> TryCatchAll<T> andGet(Throwing.Supplier<? extends T, ? extends Throwable> supplier);

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
   * @see #run(Runnable)
   */
  @Override
  public TryCatchAllVoid andRun(Throwing.Runnable<?> runnable);

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
   * @see #run(Runnable)
   */
  @Override
  public TryCatchAllVoid or(Throwing.Runnable<?> runnable);
}
