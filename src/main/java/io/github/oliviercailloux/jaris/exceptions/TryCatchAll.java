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
 * throwables. Some of these methods will catch all throwables thrown by such functional operators,
 * while others will propagate any exception thrown to the caller (see the method documentation).
 * This is the only difference between this type and the {@link Try} type: the latter catches only
 * checked exceptions instead of all throwables.
 * </p>
 * <p>
 * It is generally a bad idea to catch throwables that are not exceptions. Unless dealing with a
 * very specific use case (such as checking correctness of some code), please consider using
 * {@code Try} instead of {@code TryCatchAll}.
 * </p>
 * <p>
 * When the documentation of a method indicates that it catches checked exceptions thrown by some
 * provided functional interface, it is implicit that if the provided functional interface throws
 * anything that is not a checked exception, then it is not caught, and simply thrown back to the
 * caller.
 * </p>
 *
 * @param <T> the type of result possibly kept in the instance.
 */
public interface TryCatchAll<T>
    extends TryOptionalImpl.TryVariableCatchInterface<T, Throwable, Throwable> {
  /**
   * Returns a success containing the given result.
   *
   * @param <T> the type of result declared to be (and effectively) kept in the instance
   * @param result the result to contain
   * @return a success
   */
  public static <T> TryCatchAll<T> success(T result) {
    return TryOptionalImpl.TryCatchAllSuccess.given(result);
  }

  /**
   * Returns a failure containing the given cause.
   *
   * @param <T> the type of result declared to be (but not effectively) kept in the instance
   * @param cause the cause to contain
   * @return a failure
   */
  public static <T> TryCatchAll<T> failure(Throwable cause) {
    return TryOptionalImpl.TryCatchAllFailure.given(cause);
  }

  /**
   * Attempts to get and encapsulate a result from the given supplier.
   * <p>
   * This method returns a failure iff the given supplier throws or returns {@code null}.
   *
   * @param <T> the type of result declared to be kept in the instance
   * @param supplier the supplier to get a result from
   * @return a success containing the result if the supplier returns a result; a failure containing
   *         the throwable if the supplier throws
   */
  public static <T> TryCatchAll<T> get(TSupplier<? extends T, ?> supplier) {
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
  public abstract TryCatchAll<T> andRun(TRunnable<?> runnable);

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
  public abstract TryCatchAll<T> andConsume(TConsumer<? super T, ? extends Throwable> consumer);

  /**
   * Returns this failure if this instance is a failure; the provided failure if it is a failure and
   * this instance is a success; and a success containing the merge of the result contained in this
   * instance and the one contained in {@code t2}, if they both are successes and {@code merger}
   * does not return {@code null}.
   *
   * @param <U> the type of result that the provided try is declared to contain
   * @param <V> the type of result that the returned try will be declared to contain
   * @param <Y> a type of exception that the provided merger may throw
   * @param t2 the try to consider if this try is a success
   * @param merger the function invoked to merge the results if both this and the given try are
   *        successes
   * @return a success if this instance and the given try are two successes
   * @throws Y if the merger was applied and threw a checked exception
   * @throws NullPointerException if the merger was applied and returned {@code null}
   */
  public abstract <U, V, Y extends Exception> TryCatchAll<V> and(TryCatchAll<U> t2,
      TBiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y;

  /**
   * Returns this failure if this instance is a failure; a failure containing the cause thrown by
   * the given function if it threw a checked exception; a failure containing a
   * {@link NullPointerException} as a cause if the provided mapper returned {@code null}; or a
   * success containing the result of applying the provided mapper to the result contained in this
   * instance if it is a success and the mapper returned a non-{@code null} result.
   * <p>
   * Equivalent to {@code t.map(r -> TryCatchAll.get(() -> mapper.apply(r)), c -> t)}.
   *
   * @param <U> the type of result that the returned try will be declared to contain
   * @param mapper the mapper to apply to the result contained in this instance if it is a success
   * @return a success iff this instance is a success and the provided mapper returns a
   *         non-{@code null} result
   */
  @Override
  public abstract <U> TryCatchAll<U>
      andApply(TFunction<? super T, ? extends U, ? extends Throwable> mapper);

  /**
   * Returns this instance if it is a success, or merges this instance with the one provided by the
   * supplier.
   * <p>
   * Returns this instance if it is a success. Otherwise, attempts to get a result from the given
   * supplier. If this succeeds, that is, if the supplier returns a non-{@code null} result, returns
   * a success containing that result. Otherwise, if the supplier throws, merges both throwables
   * using the given {@code exceptionMerger} and returns a failure containing that merged cause,
   * provided it is not {@code null}. If the supplier returns {@code null}, it is treated as if it
   * had thrown a {@link NullPointerException}: this method merges the {@link NullPointerException}
   * with the cause contained in this instance using the provided {@code exceptionMerger} and
   * returns a failure containing that merged cause, provided it is not {@code null}.
   * </p>
   *
   * @param <W> a type of exception that the provided merger may throw
   * @param supplier the supplier that is invoked if this try is a failure
   * @param exceptionsMerger the function invoked to merge both exceptions if this try is a failure
   *        and the given supplier threw a checked exception
   * @return a success if this instance is a success or the given supplier returned a result
   * @throws W if the merger was applied and threw a checked exception
   */
  public abstract <W extends Exception> TryCatchAll<T> or(TSupplier<? extends T, ?> supplier,
      TBiFunction<? super Throwable, ? super Throwable, ? extends Throwable, W> exceptionsMerger)
      throws W;
}
