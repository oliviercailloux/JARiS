package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Function;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import java.util.Optional;


/**
 * An equivalent to {@link Try} that which catches all throwables instead of catching only checked
 * exceptions.
 *
 * @param <T> the type of result possibly kept in the instance.
 */
public abstract class TrySafe<T> extends TryOptionalSafe<T> implements TryGeneral<T, Throwable> {

  /**
   * Returns a success containing the given result.
   *
   * @param <T> the type of result declared to be possibly (and effectively) kept in the returned
   *        instance.
   * @param t the result to contain
   */
  public static <T> TrySafe<T> success(T t) {
    return new Success<>(t);
  }

  /**
   * Returns a failure containing the given cause.
   *
   * @param <T> the type of result declared to be possibly (but effectively not) kept in the
   *        returned instance.
   * @param cause the cause to contain
   */
  public static <T> TrySafe<T> failure(Throwable cause) {
    return new Failure<>(cause);
  }

  /**
   * Attempts to wrap a result from the given supplier.
   * <p>
   * This method returns a failure iff the given supplier throws any sort of throwable.
   *
   * @param <T> the type of result possibly kept in the returned instance.
   * @param supplier the supplier to get a result from
   * @return a success containing the result if the supplier returns a result; a failure containing
   *         the throwable if the supplier throws
   */
  public static <T> TrySafe<T> get(Throwing.Supplier<? extends T, ? extends Exception> supplier) {
    try {
      return success(supplier.get());
    } catch (Throwable x) {
      return failure(x);
    }
  }

  /**
   * Conceptually safe cast. Currently unused.
   */
  @SuppressWarnings("unused")
  private static <T> TrySafe<T> cast(TrySafe<? extends T> t) {
    @SuppressWarnings("unchecked")
    final TrySafe<T> casted = (TrySafe<T>) t;
    return casted;
  }

  private static class Success<T> extends TrySafe<T> {

    private final T result;

    private Success(T result) {
      this.result = checkNotNull(result);
    }

    @Override
    public boolean isSuccess() {
      return true;
    }

    @Override
    public boolean isFailure() {
      return false;
    }

    @Override
    Optional<T> getResult() {
      return Optional.of(result);
    }

    @Override
    Optional<Throwable> getCause() {
      return Optional.empty();
    }

    @Override
    public <U, Y extends Exception> U map(
        Throwing.Function<? super T, ? extends U, ? extends Y> transformation,
        Throwing.Function<? super Throwable, ? extends U, ? extends Y> causeTransformation)
        throws Y {
      return transformation.apply(result);
    }

    @Override
    public <Y extends Exception> T orMapCause(
        Function<? super Throwable, ? extends T, Y> causeTransformation) throws Y {
      return result;
    }

    @Override
    public <Y extends Exception> Optional<T> orConsumeCause(Consumer<? super Throwable, Y> consumer)
        throws Y {
      return Optional.of(result);
    }

    @Override
    public T orThrow() throws Throwable {
      return result;
    }

    @Override
    public <W extends Exception> TrySafe<T> or(Throwing.Supplier<? extends T, ?> supplier,
        Throwing.BiFunction<? super Throwable, ? super Throwable, ? extends Throwable, W> throwablesMerger)
        throws W {
      return this;
    }

    @Override
    public TrySafe<T> andRun(Runnable<?> runnable) {
      final TrySafeVoid ran = TrySafeVoid.run(runnable);
      return ran.map(() -> this, TrySafe::failure);
    }

    @Override
    public TrySafe<T> andConsume(Consumer<? super T, ?> consumer) {
      return andRun(() -> consumer.accept(result));
    }

    @Override
    public <U, V, Y extends Exception> TrySafe<V> and(TrySafe<U> t2,
        Throwing.BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y {
      return t2.map(u -> success(merger.apply(result, u)), TrySafe::failure);
    }

    @Override
    public <U> TrySafe<U> flatMap(Function<? super T, ? extends U, ?> mapper) {
      return TrySafe.get(() -> mapper.apply(result));
    }
  }

  private static class Failure<T> extends TrySafe<T> {
    private final Throwable cause;

    private Failure(Throwable cause) {
      this.cause = checkNotNull(cause);
    }

    @Override
    public boolean isSuccess() {
      return false;
    }

    @Override
    public boolean isFailure() {
      return true;
    }

    @Override
    Optional<T> getResult() {
      return Optional.empty();
    }

    @Override
    Optional<Throwable> getCause() {
      return Optional.of(cause);
    }

    private <U> TrySafe<U> cast() {
      @SuppressWarnings("unchecked")
      final TrySafe<U> casted = (TrySafe<U>) this;
      return casted;
    }

    @Override
    public <U, Y extends Exception> U map(
        Throwing.Function<? super T, ? extends U, ? extends Y> transformation,
        Throwing.Function<? super Throwable, ? extends U, ? extends Y> causeTransformation)
        throws Y {
      return causeTransformation.apply(cause);
    }

    @Override
    public <Y extends Exception> T orMapCause(
        Function<? super Throwable, ? extends T, Y> causeTransformation) throws Y {
      return causeTransformation.apply(cause);
    }

    @Override
    public <Y extends Exception> Optional<T> orConsumeCause(Consumer<? super Throwable, Y> consumer)
        throws Y {
      consumer.accept(cause);
      return Optional.empty();
    }

    @Override
    public T orThrow() throws Throwable {
      throw cause;
    }

    @Override
    public <W extends Exception> TrySafe<T> or(Throwing.Supplier<? extends T, ?> supplier,
        Throwing.BiFunction<? super Throwable, ? super Throwable, ? extends Throwable, W> exceptionsMerger)
        throws W {
      final TrySafe<T> t2 = TrySafe.get(supplier);
      return t2.map(TrySafe::success, y -> failure(exceptionsMerger.apply(cause, y)));
    }

    @Override
    public TrySafe<T> andRun(Runnable<?> runnable) {
      return this;
    }

    @Override
    public TrySafe<T> andConsume(Consumer<? super T, ?> consumer) {
      return this;
    }

    @Override
    public <U, V, Y extends Exception> TrySafe<V> and(TrySafe<U> t2,
        Throwing.BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y {
      return cast();
    }

    @Override
    public <U> TrySafe<U> flatMap(Function<? super T, ? extends U, ?> mapper) {
      return cast();
    }
  }

  protected TrySafe() {
    /* Reducing visibility. */
  }

  /**
   * Returns <code>true</code> iff this object contains a result (and not a cause).
   */
  @Override
  public abstract boolean isSuccess();

  /**
   * Return <code>true</code> iff this object contains a cause (and not a result).
   */
  @Override
  public abstract boolean isFailure();

  /**
   * Returns this instance if it is a success. Otherwise, attempts to get a result from the given
   * supplier. If this succeeds, that is, if the supplier returns a result, returns a success
   * containing that result. Otherwise, merges both throwables using the given
   * {@code throwablesMerger} and returns a failure containing that merged cause.
   *
   * @param <W> a type of exception that the provided merger may throw
   * @param supplier the supplier that is invoked if this try is a failure
   * @param throwablesMerger the function invoked to merge both throwables if this try is a failure
   *        and the given supplier threw
   * @return a success if this instance is a success or the given supplier returned a result
   * @throws W iff the merger was applied and threw a checked exception
   */
  public abstract <W extends Exception> TrySafe<T> or(Throwing.Supplier<? extends T, ?> supplier,
      Throwing.BiFunction<? super Throwable, ? super Throwable, ? extends Throwable, W> throwablesMerger)
      throws W;

  /**
   * Returns a failure containing this cause if this instance is a failure, a failure containing the
   * throwable that the provided runnable threw if it did throw, and a success containg the result
   * contained in this instance otherwise.
   * <p>
   * If this instance is a failure, returns this instance without running the provided runnable.
   * Otherwise, if the runnable succeeds (that is, does not throw), returns this instance.
   * Otherwise, returns a failure containing whatever the runnable threw.
   *
   * @param runnable the function to run if this instance is a success
   * @return a success iff this instance is a success and the provided runnable terminated without
   *         throwing
   */
  public abstract TrySafe<T> andRun(Throwing.Runnable<?> runnable);

  /**
   * Returns a failure containing this cause if this instance is a failure, a failure containing the
   * throwable that the provided consumer threw if it did throw, and a success containing the result
   * contained in this instance otherwise.
   * <p>
   * If this instance is a failure, returns this instance without running the provided consumer.
   * Otherwise, if the consumer succeeds (that is, does not throw), returns this instance.
   * Otherwise, if the consumer throws, returns a failure containing whatever it threw.
   *
   * @param consumer the function to run if this instance is a success
   * @return a success iff this instance is a success and the provided consumer terminated without
   *         throwing
   */
  public abstract TrySafe<T> andConsume(Throwing.Consumer<? super T, ?> consumer);

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
  public abstract <U, V, Y extends Exception> TrySafe<V> and(TrySafe<U> t2,
      Throwing.BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y;

  /**
   * Returns this failure if this instance is a failure; a failure containing the cause thrown by
   * the given function if it threw; or a success containing the result of applying the provided
   * mapper to the result contained in this instance if it is a success and the mapper did not
   * throw.
   *
   * @param <U> the type of result that the returned try will be declared to contain
   * @param mapper the mapper to apply to the result contained in this instance if it is a success
   * @return a success iff this instance is a success and the provided mapper does not throw
   */
  public abstract <U> TrySafe<U> flatMap(Throwing.Function<? super T, ? extends U, ?> mapper);

  @Override
  public String toString() {
    final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
    orConsumeCause(e -> stringHelper.add("cause", e)).ifPresent(r -> stringHelper.add("result", r));
    return stringHelper.toString();
  }

}
