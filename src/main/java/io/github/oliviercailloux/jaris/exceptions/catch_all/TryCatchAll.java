package io.github.oliviercailloux.jaris.exceptions.catch_all;

import io.github.oliviercailloux.jaris.exceptions.Throwing;
import io.github.oliviercailloux.jaris.exceptions.catch_all.impl.TryCatchAllImpl.TryCatchAllFailure;
import io.github.oliviercailloux.jaris.exceptions.catch_all.impl.TryCatchAllImpl.TryCatchAllSuccess;
import io.github.oliviercailloux.jaris.exceptions.impl.TryOptional;

public interface TryCatchAll<T>
    extends TryOptional.TryVariableCatchInterface<T, Throwable, Throwable> {
  public static <T> TryCatchAll<T> success(T result) {
    return TryCatchAllSuccess.given(result);
  }

  public static <T> TryCatchAll<T> failure(Throwable cause) {
    return TryCatchAllFailure.given(cause);
  }

  public static <T> TryCatchAll<T> get(Throwing.Supplier<? extends T, ?> supplier) {
    try {
      return success(supplier.get());
    } catch (Throwable e) {
      return failure(e);
    }
  }

  /**
   * Returns a failure containing this cause if this instance is a failure, a failure containing the
   * checked exception that the provided runnable threw if it did throw one, and a success containg
   * the result contained in this instance if this instance is a success and the provided runnable
   * does not throw.
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
  public abstract TryCatchAll<T> andRun(Throwing.Runnable<?> runnable);

  /**
   * Returns a failure containing this cause if this instance is a failure, a failure containing the
   * checked exception that the provided consumer threw if it did throw one, and a success containg
   * the result contained in this instance otherwise.
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
  public abstract TryCatchAll<T> andConsume(Throwing.Consumer<? super T, ?> consumer);

  /**
   * Returns this failure if this instance is a failure; the provided failure if it is a failure and
   * this instance is a success; and a success containing the merge of the result contained in this
   * instance and the one contained in {@code t2}, if they both are successes.
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
  public abstract <U, V, Y extends Exception> TryCatchAll<V> and(TryCatchAll<U> t2,
      Throwing.BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y;

  /**
   * Returns this failure if this instance is a failure; a failure containing the cause thrown by
   * the given function if it threw a checked exception; or a success containing the result of
   * applying the provided mapper to the result contained in this instance if it is a success and
   * the mapper did not throw.
   *
   * @param <U> the type of result that the returned try will be declared to contain
   * @param mapper the mapper to apply to the result contained in this instance if it is a success
   * @return a success iff this instance is a success and the provided mapper does not throw
   */
  @Override
  public abstract <U> TryCatchAll<U> andApply(
      Throwing.Function<? super T, ? extends U, ? extends Throwable> mapper);

  /**
   * Returns this instance if it is a success. Otherwise, attempts to get a result from the given
   * supplier. If this succeeds, that is, if the supplier returns a result, returns a success
   * containing that result. Otherwise, if the supplier throws a checked exception, merges both
   * exceptions using the given {@code exceptionMerger} and returns a failure containing that merged
   * cause.
   *
   * @param <W> a type of exception that the provided merger may throw
   * @param supplier the supplier that is invoked if this try is a failure
   * @param exceptionsMerger the function invoked to merge both exceptions if this try is a failure
   *        and the given supplier threw a checked exception
   * @return a success if this instance is a success or the given supplier returned a result
   * @throws W iff the merger was applied and threw a checked exception
   */
  public abstract <W extends Exception> TryCatchAll<T> or(
      Throwing.Supplier<? extends T, ?> supplier,
      Throwing.BiFunction<? super Throwable, ? super Throwable, ? extends Throwable, W> exceptionsMerger)
      throws W;
}
