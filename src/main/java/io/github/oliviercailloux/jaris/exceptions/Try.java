package io.github.oliviercailloux.jaris.exceptions;

/**
 * TODO simplify hierarchy: we have 21 classes, that’s far too much for avoiding only a bit of
 * redundancy. Simplifiy equality: classes should only equalize inter class. Make wo interfaces,
 * TryStatic and TryStaticVoid, with the few main methods not involving catch. Then interfaces Try
 * and TryCatchAll; TryVoid and TryCatchAllVoid. Then TryImpl with two inner classes, and similarly
 * for the other ones. That’s 6 interfaces + 12 classes = 18 classes. Not much better.
 *
 * @param <T>
 * @param <X>
 */
public interface Try<T, X extends Exception>
    extends TryOptional.TryVariableCatchInterface<T, X, Exception> {
  public static <T, X extends Exception> Try<T, X> success(T result) {
    return TryOptional.TrySuccess.given(result);
  }

  public static <T, X extends Exception> Try<T, X> failure(X cause) {
    return TryOptional.TryFailure.given(cause);
  }

  public static <T, X extends Exception> Try<T, X> get(
      Throwing.Supplier<? extends T, ? extends X> supplier) {
    try {
      return success(supplier.get());
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      @SuppressWarnings("unchecked")
      final X exc = (X) e;
      return failure(exc);
    }
  }

  /**
   * Returns this instance if it is a success. Otherwise, attempts to get a result from the given
   * supplier. If this succeeds, that is, if the supplier returns a result, returns a success
   * containing that result. Otherwise, if the supplier throws a checked exception, merges both
   * exceptions using the given {@code exceptionMerger} and returns a failure containing that merged
   * cause.
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
  public abstract <Y extends Exception, Z extends Exception, W extends Exception> Try<T, Z> or(
      Throwing.Supplier<? extends T, Y> supplier,
      Throwing.BiFunction<? super X, ? super Y, ? extends Z, W> exceptionsMerger) throws W;

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
  public abstract Try<T, X> andRun(Throwing.Runnable<? extends X> runnable);

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
  public abstract Try<T, X> andConsume(Throwing.Consumer<? super T, ? extends X> consumer);

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
  public abstract <U, V, Y extends Exception> Try<V, X> and(Try<U, ? extends X> t2,
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
  public abstract <U> Try<U, X> flatMap(
      Throwing.Function<? super T, ? extends U, ? extends X> mapper);
}
