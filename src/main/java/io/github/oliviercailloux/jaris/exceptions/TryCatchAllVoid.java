package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;

public interface TryCatchAllVoid
    extends TryOptional.TryVoidVariableCatchInterface<Throwable, Throwable> {
  public static TryCatchAllVoid success() {
    return TryVoidCatchAllSuccess.given();
  }

  public static TryCatchAllVoid failure(Throwable cause) {
    return TryVoidCatchAllFailure.given(cause);
  }

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
   * @param <X> the type of cause that the returned try will be declared to contain
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
   * @param <X> the type of cause that the returned instance will be declared to contain
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
   * @param <X> the type of cause that the returned instance will be declared to contain
   * @param runnable the runnable to attempt to run if this instance is a failure.
   * @return a success iff this instance is a success or the given runnable terminated without
   *         throwing.
   * @see #run(Runnable)
   */
  @Override
  public TryCatchAllVoid or(Throwing.Runnable<?> runnable);
}
