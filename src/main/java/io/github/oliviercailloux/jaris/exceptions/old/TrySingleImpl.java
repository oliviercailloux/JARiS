package io.github.oliviercailloux.jaris.exceptions.old;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import io.github.oliviercailloux.jaris.exceptions.Throwing;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Function;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import java.util.Optional;


/**
 * <p>
 * An instance of this class contains either a result, in which case it is called a “success”; or a
 * cause of type {@link Exception}, in which case it is called a “failure”.
 * </p>
 * <p>
 * Instances of this class are immutable.
 * </p>
 * <p>
 * Heavily inspired by <a href="https://github.com/vavr-io/vavr">Vavr</a>. One notable difference is
 * that this class (and this library) does not sneaky throw (see the contract of Vavr’s
 * <code>Try#<a href=
 * "https://github.com/vavr-io/vavr/blob/9a40af5cec2622a8ce068d5833a2bf07671f5eed/src/main/java/io/vavr/control/Try.java#L629">get()</a></code>
 * and its <a href=
 * "https://github.com/vavr-io/vavr/blob/9a40af5cec2622a8ce068d5833a2bf07671f5eed/src/main/java/io/vavr/control/Try.java#L1305">implementation</a>).
 * <p>
 * Can be safe (catch-all) or unsafe (catch-checked)
 *
 * @param <T> the type of result possibly kept in the instance.
 * @param <X> the type of cause possibly kept in the instance.
 */
abstract class TrySingleImpl<T, X extends TE, TE extends Throwable>
    extends TrySingleImplOptional<T, X> {

  static <T> Success<T, Throwable, Throwable> successCatchAll(T t) {
    return new Success<>(t, true);
  }

  static <T, X extends Exception> Success<T, X, Exception> successCatchChecked(T t) {
    return new Success<>(t, false);
  }

  static <T> Failure<T, Throwable, Throwable> failureCatchAll(Throwable cause) {
    return new Failure<>(cause, true);
  }

  static <T, X extends Exception> Failure<T, X, Exception> failureCatchChecked(X cause) {
    return new Failure<>(cause, false);
  }

  private static class Success<T, X extends TE, TE extends Throwable>
      extends TrySingleImpl<T, X, TE> {

    private final T result;

    private Success(T result, boolean catchAll) {
      super(catchAll);
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
    Optional<X> getCause() {
      return Optional.empty();
    }

    private <Y extends TE> TrySingleImpl<T, Y, TE> cast() {
      @SuppressWarnings("unchecked")
      final TrySingleImpl<T, Y, TE> casted = (TrySingleImpl<T, Y, TE>) this;
      return casted;
    }

    @Override
    public <U, Y extends TE> U map(
        Throwing.Function<? super T, ? extends U, ? extends Y> transformation,
        Throwing.Function<? super X, ? extends U, ? extends Y> causeTransformation) throws Y {
      return transformation.apply(result);
    }

    @Override
    public <Y extends Exception> T orMapCause(
        Function<? super X, ? extends T, Y> causeTransformation) throws Y {
      return result;
    }

    @Override
    public <Y extends Exception> Optional<T> orConsumeCause(Consumer<? super X, Y> consumer)
        throws Y {
      return Optional.of(result);
    }

    @Override
    public T orThrow() throws X {
      return result;
    }

    @Override
    public <Y extends TE, Z extends TE, W extends Exception> TrySingleImpl<T, Z, TE> or(
        Throwing.Supplier<? extends T, Y> supplier,
        Throwing.BiFunction<? super X, ? super Y, ? extends Z, W> exceptionsMerger) throws W {
      return cast();
    }

    @Override
    public TrySingleImpl<T, X, TE> andRun(Runnable<? extends X> runnable) {
      final TryVoid<? extends X> ran = TryVoid.run(runnable);
      return ran.map(() -> this, TrySingleImpl::failure);
    }

    @Override
    public TrySingleImpl<T, X> andConsume(Consumer<? super T, ? extends X> consumer) {
      return andRun(() -> consumer.accept(result));
    }

    @Override
    public <U, V, Y extends Exception> TrySingleImpl<V, X, TE> and(
        TrySingleImpl<U, ? extends X, TE> t2,
        Throwing.BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y {
      final Builder<X, TE> builder;
      return t2.map(u -> builder.success(merger.apply(result, u)), builder::failure);
    }

    @Override
    public <U> TrySingleImpl<U, X, TE> flatMap(
        Function<? super T, ? extends U, ? extends X> mapper) {
      final Builder<X, TE> builder;
      return builder.get(() -> mapper.apply(result));
    }
  }

  private static class Failure<T, X extends TE, TE extends Throwable>
      extends TrySingleImpl<T, X, TE> {
    private final X cause;

    private Failure(X cause, boolean catchAll) {
      super(catchAll);
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
    Optional<X> getCause() {
      return Optional.of(cause);
    }

    private <U> TrySingleImpl<U, X, TE> cast() {
      @SuppressWarnings("unchecked")
      final TrySingleImpl<U, X, TE> casted = (TrySingleImpl<U, X, TE>) this;
      return casted;
    }

    @Override
    public <U, Y extends TE> U map(
        Throwing.Function<? super T, ? extends U, ? extends Y> transformation,
        Throwing.Function<? super X, ? extends U, ? extends Y> causeTransformation) throws Y {
      return causeTransformation.apply(cause);
    }

    @Override
    public <Y extends Exception> T orMapCause(
        Function<? super X, ? extends T, Y> causeTransformation) throws Y {
      return causeTransformation.apply(cause);
    }

    @Override
    public <Y extends Exception> Optional<T> orConsumeCause(Consumer<? super X, Y> consumer)
        throws Y {
      consumer.accept(cause);
      return Optional.empty();
    }

    @Override
    public T orThrow() throws X {
      throw cause;
    }

    @Override
    public <Y extends TE, Z extends TE, W extends Exception> TrySingleImpl<T, Z, TE> or(
        Throwing.Supplier<? extends T, Y> supplier,
        Throwing.BiFunction<? super X, ? super Y, ? extends Z, W> exceptionsMerger) throws W {
      final TrySingleImpl<T, Y> t2 = TrySingleImpl.get(supplier);
      return t2.map(TrySingleImpl::success, y -> failure(exceptionsMerger.apply(cause, y)));
    }

    @Override
    public TrySingleImpl<T, X, TE> andRun(Runnable<? extends X> runnable) {
      return this;
    }

    @Override
    public TrySingleImpl<T, X, TE> andConsume(Consumer<? super T, ? extends X> consumer) {
      return this;
    }

    @Override
    public <U, V, Y extends Exception> TrySingleImpl<V, X, TE> and(
        TrySingleImpl<U, ? extends X, TE> t2,
        Throwing.BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y {
      return cast();
    }

    @Override
    public <U> TrySingleImpl<U, X, TE> flatMap(
        Function<? super T, ? extends U, ? extends X> mapper) {
      return cast();
    }
  }

  protected TrySingleImpl(boolean catchAll) {
    super(catchAll);
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
   * Returns the transformed result contained in this instance if it is a success, using the
   * provided {@code transformation}; or the transformed cause contained in this instance if it is a
   * failure, using the provided {@code causeTransformation}.
   * <p>
   * This method necessarily applies exactly one of the provided functions.
   *
   * @param <U> the type of transformed result to return
   * @param <Y> a type of exception that the provided functions may throw
   * @param transformation a function to apply to the result if this instance is a success
   * @param causeTransformation a function to apply to the cause if this instance is a failure
   * @return the transformed result or cause
   * @throws Y iff the function that was applied threw a checked exception
   */
  public abstract <U, Y extends TE> U map(
      Throwing.Function<? super T, ? extends U, ? extends Y> transformation,
      Throwing.Function<? super X, ? extends U, ? extends Y> causeTransformation) throws Y;

  /**
   * Returns the result contained in this instance if it is a success, without applying the provided
   * function; or returns the transformed cause contained in this instance if it is a failure, using
   * the provided {@code causeTransformation}.
   * <p>
   * Equivalent to: {@code map(Function#identity(), causeTransformation)}.
   *
   * @param <Y> a type of exception that the provided function may throw
   * @param causeTransformation the function to apply if this instance is a failure
   * @return the result, or the transformed cause
   * @throws Y iff the function was applied and threw a checked exception
   */
  public abstract <Y extends Exception> T orMapCause(
      Throwing.Function<? super X, ? extends T, Y> causeTransformation) throws Y;

  /**
   * Returns an optional containing the result of this instance, without invoking the given
   * consumer, if this try is a success; otherwise, invokes the given consumer and returns an empty
   * optional.
   *
   * @param <Y> a type of exception that the provided consumer may throw
   * @param consumer the consumer to invoke if this instance is a failure
   * @return an optional, containing the result if this instance is a success, empty otherwise
   * @throws Y iff the consumer was invoked and threw a checked exception
   */
  public abstract <Y extends Exception> Optional<T> orConsumeCause(
      Throwing.Consumer<? super X, Y> consumer) throws Y;

  /**
   * Returns the result contained in this instance if this instance is a success, or throws the
   * cause contained in this instance.
   *
   * @return the result that this success contains
   * @throws X iff this instance is a failure
   */
  public abstract T orThrow() throws X;

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
  public abstract <Y extends TE, Z extends TE, W extends Exception> TrySingleImpl<T, Z, TE> or(
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
  public abstract TrySingleImpl<T, X, TE> andRun(Throwing.Runnable<? extends X> runnable);

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
  public abstract TrySingleImpl<T, X, TE> andConsume(
      Throwing.Consumer<? super T, ? extends X> consumer);

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
  public abstract <U, V, Y extends Exception> TrySingleImpl<V, X, TE> and(
      TrySingleImpl<U, ? extends X, TE> t2,
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
  public abstract <U> TrySingleImpl<U, X, TE> flatMap(
      Throwing.Function<? super T, ? extends U, ? extends X> mapper);

  @Override
  public String toString() {
    final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
    orConsumeCause(e -> stringHelper.add("cause", e)).ifPresent(r -> stringHelper.add("result", r));
    return stringHelper.toString();
  }

}
