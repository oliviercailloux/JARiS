package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import io.github.oliviercailloux.jaris.throwing.TBiFunction;
import io.github.oliviercailloux.jaris.throwing.TConsumer;
import io.github.oliviercailloux.jaris.throwing.TFunction;
import io.github.oliviercailloux.jaris.throwing.TRunnable;
import io.github.oliviercailloux.jaris.throwing.TSupplier;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * See this class in branch doc-try for some thoughts about possible extensions; and extensive
 * (draft!) documentation about the design choices.
 *
 * @param <T> the type of result possibly kept in this object.
 * @param <X> the type of cause kept in this object if it is a failure.
 */
abstract class TryOptionalImpl<T, X extends Throwable> implements TryOptional<T, X> {

  /**
   * A sort of try optional that guarantees that a success has a (non-{@code null}) associated
   * value. Is homeomorphic to a {@code T} xor {@code X}. Suitable for {@link Try} and
   * {@link TryCatchAll}, depending on the catching strategy. The name (“variable catch”) indicates
   * that this interface applies to both catching strategies.
   *
   * @param <T> the type of result kept in this object if it is a success.
   * @param <X> the type of cause kept in this object if it is a failure.
   * @param <Z> a priori constraint applied to some functionals on the type of throwable that they
   *        may throw – when catching all, it sometimes makes sense to authorize functionals to
   *        throw {@code Throwable}; when catching species of exceptions, this makes no sense and we
   *        reduce possible signatures to clarify the intended use.
   */
  public interface TryVariableCatchInterface<T, X extends Z, Z extends Throwable>
      extends TryOptional<T, X> {

    /**
     * Returns {@code true} iff this instance contains a result (and not a cause).
     *
     * @return {@code true} iff {@link #isFailure()} returns {@code false}
     */
    @Override
    public boolean isSuccess();

    /**
     * Return {@code true} iff this instance contains a cause (and not a result).
     *
     * @return {@code true} iff {@link #isSuccess()} returns {@code false}
     */
    @Override
    public boolean isFailure();

    /**
     * Returns the transformed result contained in this instance if it is a success, using the
     * provided {@code transformation}; or the transformed cause contained in this instance if it is
     * a failure, using the provided {@code causeTransformation}.
     * <p>
     * This method necessarily applies exactly one of the provided functions.
     *
     * @param <U> the type of transformed result to return
     * @param <Y> a type of exception that the provided functions may throw
     * @param transformation a function to apply to the result if this instance is a success
     * @param causeTransformation a function to apply to the cause if this instance is a failure
     * @return the transformed result or cause
     * @throws Y if the function that was applied threw an exception of type {@code Y}
     * @throws NullPointerException if the function that was applied returned {@code null}
     */
    public <U, Y extends Exception> U map(
        TFunction<? super T, ? extends U, ? extends Y> transformation,
        TFunction<? super X, ? extends U, ? extends Y> causeTransformation) throws Y;

    /**
     * Returns the result contained in this instance if it is a success, without applying the
     * provided function; or returns the transformed cause contained in this instance if it is a
     * failure, using the provided {@code causeTransformation}.
     * <p>
     * Equivalent to {@code map(t -> t, causeTransformation)}.
     *
     * @param <Y> a type of exception that the provided function may throw
     * @param causeTransformation the function to apply iff this instance is a failure
     * @return the result, or the transformed cause
     * @throws Y if the function was applied and threw an exception of type {@code Y}
     * @throws NullPointerException if the function was applied and returned {@code null}
     */
    public <Y extends Exception> T
        orMapCause(TFunction<? super X, ? extends T, Y> causeTransformation) throws Y;

    /**
     * Returns an optional containing the result of this instance, without invoking the given
     * consumer, if this try is a success; otherwise, invokes the given consumer and returns an
     * empty optional.
     *
     * @param <Y> a type of exception that the provided consumer may throw
     * @param consumer the consumer to invoke if this instance is a failure
     * @return an optional, containing the result if this instance is a success, empty otherwise
     * @throws Y if the consumer was invoked and threw an exception of type {@code Y}
     */
    public <Y extends Exception> Optional<T>
        orConsumeCause(TConsumer<? super X, Y> consumer) throws Y;

    /**
     * Returns the result contained in this instance if this instance is a success, or throws the
     * cause contained in this instance.
     * <p>
     * Equivalent to: {@link #orThrow(Function) orThrow(t -> t)}.
     *
     * @return the result that this success contains
     * @throws X iff this instance is a failure
     */
    public T orThrow() throws X;

    /**
     * Returns the result contained in this instance if this instance is a success, or throws the
     * transformed cause contained in this instance.
     *
     * @param <Y> the type of throwable to throw if this instance is a failure
     * @param causeTransformation the function to apply to the cause iff this instance is a failure
     * @return the result that this success contains
     * @throws Y if this instance is a failure
     * @throws NullPointerException if the provided function was applied and returned {@code null}
     */
    public <Y extends Z> T orThrow(Function<X, Y> causeTransformation) throws Y;

    /**
     * Runs the runnable iff this instance is a success, and returns this instance if it succeeds
     * and the cause of failure if it throws a catchable throwable; otherwise, returns this
     * instance.
     *
     * @param runnable the runnable to invoke iff this instance is a success
     * @return a success iff this instance is a success and the provided runnable does not throw
     */
    public TryVariableCatchInterface<T, X, Z> andRun(TRunnable<? extends X> runnable);

    /**
     * Runs the consumer iff this instance is a success, and returns this instance if it succeeds
     * and the cause of failure if it throws a catchable throwable; otherwise, returns this
     * instance.
     *
     * @param consumer the consumer to invoke iff this instance is a success
     * @return a success iff this instance is a success and the provided consumer does not throw
     */
    public TryVariableCatchInterface<T, ?, Z>
        andConsume(TConsumer<? super T, ? extends X> consumer);

    /**
     * Applies the given mapper iff this instance is a success, and returns the transformed success
     * if it returns a non-{@code null} value, the cause of the failure if it throws a catchable
     * throwable (including possibly a {@link NullPointerException} cause if this is considered
     * catchable and the function returns {@code null}); otherwise, returns this instance.
     * <p>
     * Equivalent to {@code t.map(s -> Try.get(() -> mapper.apply(s)), t)}.
     *
     * @param <U> the type of result that the returned try will be declared to contain
     * @param mapper the mapper to apply to the result iff this instance is a success
     * @return a success iff this instance is a success and the provided mapper does not throw
     */
    public <U> TryVariableCatchInterface<U, X, Z>
        andApply(TFunction<? super T, ? extends U, ? extends X> mapper);

    /**
     * Returns {@code true} iff the given object is a {@code TryOptional}, this and the given object
     * are both successes and have equal results, or are both failures and have equal causes.
     *
     * @param o2 the object to compare this instance to
     * @return {@code true} iff {@code o2} is a {@code TryOptional} and this instance and {@code o2}
     *         have present and equal results or present and equal causes
     */
    @Override
    public boolean equals(Object o2);
  }

  /**
   * A sort of try optional that guarantees that a success has no associated value. Is homeomorphic
   * to an {@code Optional<X>} (plus indication of catching checked or catching all). Suitable for
   * {@link TryVoid} and {@link TryCatchAllVoid}, depending on the catching strategy. The name
   * (“variable catch”) indicates that this interface applies to both catching strategies.
   *
   * @param <X> the type of cause kept in this object if it is a failure.
   * @param <Z> a priori constraint applied to some functionals on the type of throwable that they
   *        may throw (see {@link TryVariableCatchInterface}).
   */
  public interface TryVariableCatchVoidInterface<X extends Z, Z extends Throwable>
      extends TryOptional<Void, X> {

    /**
     * Returns {@code true} iff this instance is a success, hence, contains no cause.
     *
     * @return {@code true} iff {@link #isFailure()} returns {@code false}
     */
    @Override
    public boolean isSuccess();

    /**
     * Return {@code true} iff this instance contains a cause.
     *
     * @return {@code true} iff {@link #isSuccess()} returns {@code false}
     */
    @Override
    public boolean isFailure();

    /**
     * Returns the supplied result if this instance is a success, using the provided
     * {@code supplier}; or the transformed cause contained in this instance if it is a failure,
     * using the provided {@code causeTransformation}; unless the invoked function returns
     * {@code null}.
     * <p>
     * This method necessarily invokes exactly one of the provided functional interfaces.
     *
     * @param <T> the type of (supplied or transformed) result to return
     * @param <Y> a type of exception that the provided functions may throw
     * @param supplier a supplier to get a result from if this instance is a success
     * @param causeTransformation a function to apply to the cause if this instance is a failure
     * @return the supplied result or transformed cause
     * @throws Y iff the functional interface that was invoked threw an exception of type {@code Y}
     * @throws NullPointerException if the function that was applied returned {@code null}
     */
    public <T, Y extends Exception> T map(TSupplier<? extends T, ? extends Y> supplier,
        TFunction<? super X, ? extends T, ? extends Y> causeTransformation) throws Y;

    /**
     * If this instance is a failure, invokes the given consumer using the cause contained in this
     * instance. If this instance is a success, do nothing.
     *
     * @param <Y> a type of exception that the provided consumer may throw
     * @param consumer the consumer to invoke if this instance is a failure
     * @throws Y iff the consumer was invoked and threw an exception of type {@code Y}
     */
    public <Y extends Exception> void ifFailed(TConsumer<? super X, Y> consumer) throws Y;

    /**
     * If this instance is a failure, throws the cause it contains. Otherwise, do nothing.
     * <p>
     * Equivalent to: {@link #orThrow(Function) orThrow(t -> t)}.
     *
     * @throws X iff this instance contains a cause
     */
    public void orThrow() throws X;

    /**
     * If this instance is a failure, throws the transformed cause it contains. Otherwise, do
     * nothing.
     *
     * @param <Y> the type of throwable to throw if this instance is a failure
     * @param causeTransformation the function to apply to the cause iff this instance is a failure
     * @throws Y if this instance is a failure
     * @throws NullPointerException if the provided function was applied and returned {@code null}
     */
    public <Y extends Z> void orThrow(Function<X, Y> causeTransformation) throws Y;

    /**
     * If this instance is a success, returns a try representing the result of invoking the given
     * supplier if it supplies a non-{@code null} result, and a failure if the supplier throws a
     * catchable throwable (or if the supplier returns {@code null} and a
     * {@link NullPointerException} is considered catchable); otherwise, returns this failure.
     * <p>
     * If this instance is a failure, it is returned, without invoking the given supplier.
     * Otherwise, the given supplier is invoked. If it supplies a non-{@code null} result, a success
     * is returned, containing the just supplied result. If the supplier throws a catchable
     * throwable, a failure is returned, containing the cause it threw. If the supplier returns
     * {@code null} and a {@link NullPointerException} is considered catchable, a failure is
     * returned, containing a {@link NullPointerException} as a cause.
     *
     * @param <T> the type of result that the returned try will be declared to contain
     * @param supplier the supplier to attempt to get a result from if this instance is a success.
     * @return a success iff this instance is a success and the given supplier returned a
     *         non-{@code null} result.
     */
    public <T> TryVariableCatchInterface<T, X, Z>
        andGet(TSupplier<? extends T, ? extends X> supplier);

    /**
     * If this instance is a success, returns a try void representing the result of invoking the
     * given runnable; otherwise, returns this failure.
     * <p>
     * If this instance is a failure, it is returned, without invoking the given runnable.
     * Otherwise, the given runnable is invoked. If it terminates without throwing, a success is
     * returned. If the runnable throws a catchable throwable, a failure is returned, containing the
     * cause it threw.
     *
     * @param runnable the runnable to attempt to run if this instance is a success.
     * @return a success iff this instance is a success and the given runnable terminated without
     *         throwing.
     */
    public TryVariableCatchVoidInterface<X, Z> andRun(TRunnable<? extends X> runnable);

    /**
     * Returns this instance if it is a success; otherwise, returns a try void representing the
     * result of invoking the given runnable.
     * <p>
     * If this instance is a success, it is returned, without invoking the given runnable.
     * Otherwise, the given runnable is invoked. If it terminates without throwing, a success is
     * returned. If the runnable throws a catchable throwable, a failure is returned, containing the
     * cause it threw.
     *
     * @param runnable the runnable to attempt to invoke if this instance is a failure.
     * @return a success iff this instance is a success or the given runnable terminated without
     *         throwing.
     */
    public TryVariableCatchVoidInterface<X, Z> or(TRunnable<? extends X> runnable);

    /**
     * Returns {@code true} iff the given object is a {@code TryOptional}, this and the given object
     * are both successes, or are both failures and have equal causes.
     *
     * @param o2 the object to compare this instance to
     * @return {@code true} iff {@code o2} is a {@code TryOptional} and this instance and {@code o2}
     *         are both successes or have present and equal causes
     */
    @Override
    public boolean equals(Object o2);
  }

  public abstract static class TryVariableCatch<T, X extends Z, Z extends Throwable>
      extends TryOptionalImpl<T, X> implements TryVariableCatchInterface<T, X, Z> {
    @Override
    public <Y extends Exception> T
        orMapCause(TFunction<? super X, ? extends T, Y> causeTransformation) throws Y {
      return map(t -> t, causeTransformation);
    }

    @Override
    public T orThrow() throws X {
      return orThrow(t -> t);
    }

    @Override
    public String toString() {
      final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
      orConsumeCause(e -> stringHelper.add("cause", e))
          .ifPresent(r -> stringHelper.add("result", r));
      return stringHelper.toString();
    }
  }

  public abstract static class TryVariableCatchSuccess<T, X extends Z, Z extends Throwable>
      extends TryVariableCatch<T, X, Z> {

    protected final T result;

    protected TryVariableCatchSuccess(T result) {
      this.result = checkNotNull(result);
    }

    @Override
    public Optional<T> getResult() {
      return Optional.of(result);
    }

    @Override
    public Optional<X> getCause() {
      return Optional.empty();
    }

    @Override
    public <U, Y extends Exception> U map(
        TFunction<? super T, ? extends U, ? extends Y> transformation,
        TFunction<? super X, ? extends U, ? extends Y> causeTransformation) throws Y {
      final U transformed = transformation.apply(result);
      checkNotNull(transformed, "Transformation returned null");
      return transformed;
    }

    @Override
    public <Y extends Exception> Optional<T>
        orConsumeCause(TConsumer<? super X, Y> consumer) throws Y {
      return Optional.of(result);
    }

    @Override
    public <Y extends Z> T orThrow(Function<X, Y> causeTransformation) {
      return result;
    }
  }

  public abstract static class TryVariableCatchFailure<X extends Z, Z extends Throwable>
      extends TryVariableCatch<Object, X, Z> {

    protected final X cause;

    protected TryVariableCatchFailure(X cause) {
      this.cause = checkNotNull(cause);
    }

    @Override
    public Optional<Object> getResult() {
      return Optional.empty();
    }

    @Override
    public Optional<X> getCause() {
      return Optional.of(cause);
    }

    @Override
    public <U, Y extends Exception> U map(
        TFunction<? super Object, ? extends U, ? extends Y> transformation,
        TFunction<? super X, ? extends U, ? extends Y> causeTransformation) throws Y {
      final U transformed = causeTransformation.apply(cause);
      checkNotNull(transformed, "Cause transformation returned null");
      return transformed;
    }

    @Override
    public <Y extends Exception> Optional<Object>
        orConsumeCause(TConsumer<? super X, Y> consumer) throws Y {
      consumer.accept(cause);
      return Optional.empty();
    }

    @Override
    public <Y extends Z> Object orThrow(Function<X, Y> causeTransformation) throws Y {
      final Y transformed = causeTransformation.apply(cause);
      checkNotNull(transformed, "Cause transformation returned null");
      throw transformed;
    }
  }

  public abstract static class TryVariableCatchVoid<X extends Z, Z extends Throwable>
      extends TryOptionalImpl<Void, X> implements TryVariableCatchVoidInterface<X, Z> {
    @Override
    public Optional<Void> getResult() {
      return Optional.empty();
    }

    @Override
    public void orThrow() throws X {
      orThrow(t -> t);
    }

    @Override
    public String toString() {
      final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
      andRun(() -> stringHelper.addValue("success"));
      ifFailed(e -> stringHelper.add("cause", e));
      return stringHelper.toString();
    }
  }

  public abstract static class TryVariableCatchVoidSuccess<X extends Z, Z extends Throwable>
      extends TryVariableCatchVoid<X, Z> {

    protected TryVariableCatchVoidSuccess() {
      /* Reducing visibility. */
    }

    @Override
    public Optional<X> getCause() {
      return Optional.empty();
    }

    @Override
    public <T, Y extends Exception> T map(TSupplier<? extends T, ? extends Y> supplier,
        TFunction<? super X, ? extends T, ? extends Y> causeTransformation) throws Y {
      final T supplied = supplier.get();
      checkNotNull(supplied, "Supplied null");
      return supplied;
    }

    @Override
    public <Y extends Exception> void ifFailed(TConsumer<? super X, Y> consumer) throws Y {
      /* Nothing to do. */
    }

    @Override
    public <Y extends Z> void orThrow(Function<X, Y> causeTransformation) {
      /* Nothing to do. */
    }
  }

  public abstract static class TryVariableCatchVoidFailure<X extends Z, Z extends Throwable>
      extends TryVariableCatchVoid<X, Z> {

    protected final X cause;

    protected TryVariableCatchVoidFailure(X cause) {
      this.cause = checkNotNull(cause);
    }

    @Override
    public Optional<X> getCause() {
      return Optional.of(cause);
    }

    @Override
    public <T, Y extends Exception> T map(TSupplier<? extends T, ? extends Y> supplier,
        TFunction<? super X, ? extends T, ? extends Y> causeTransformation) throws Y {
      final T transformed = causeTransformation.apply(cause);
      checkNotNull(transformed, "Cause transformation returned null");
      return transformed;
    }

    @Override
    public <Y extends Exception> void ifFailed(TConsumer<? super X, Y> consumer) throws Y {
      consumer.accept(cause);
    }

    @Override
    public <Y extends Z> void orThrow(Function<X, Y> causeTransformation) throws Y {
      final Y transformed = causeTransformation.apply(cause);
      checkNotNull(transformed, "Cause transformation returned null");
      throw transformed;
    }
  }

  public static class TrySuccess<T> extends TryVariableCatchSuccess<T, Exception, Exception>
      implements Try<T, Exception> {
    public static <T, X extends Exception> Try<T, X> given(T result) {
      return new TrySuccess<>(result).cast();
    }

    private TrySuccess(T result) {
      super(result);
    }

    private <Y extends Exception> Try<T, Y> cast() {
      /*
       * Safe: there is no cause in this (immutable) instance, thus its declared type does not
       * matter.
       */
      @SuppressWarnings("unchecked")
      final Try<T, Y> casted = (Try<T, Y>) this;
      return casted;
    }

    @Override
    public Try<T, Exception> andRun(TRunnable<? extends Exception> runnable) {
      final TryVoid<? extends Exception> ran = TryVoid.run(runnable);
      return ran.map(() -> this, Try::failure);
    }

    @Override
    public Try<T, Exception>
        andConsume(TConsumer<? super T, ? extends Exception> consumer) {
      return andRun(() -> consumer.accept(result));
    }

    @Override
    public <U, V, Y extends Exception> Try<V, Exception> and(Try<U, ? extends Exception> t2,
        TBiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y {
      return t2.map(u -> Try.success(merger.apply(result, u)), Try::failure);
    }

    @Override
    public <U> Try<U, Exception>
        andApply(TFunction<? super T, ? extends U, ? extends Exception> mapper) {
      return Try.get(() -> mapper.apply(result));
    }

    @Override
    public <Y extends Exception, Z extends Exception, W extends Exception> Try<T, Z> or(
        TSupplier<? extends T, Y> supplier,
        TBiFunction<? super Exception, ? super Y, ? extends Z, W> exceptionsMerger)
        throws W {
      return cast();
    }
  }

  public static class TryFailure<X extends Exception>
      extends TryOptionalImpl.TryVariableCatchFailure<X, Exception> implements Try<Object, X> {
    public static <T, X extends Exception> Try<T, X> given(X cause) {
      return new TryFailure<>(cause).cast();
    }

    private TryFailure(X cause) {
      super(cause);
    }

    private <U> Try<U, X> cast() {
      /*
       * We can thus have a declared Try<String, X> t which will be treated as a Try<Object, X> by
       * this class. This is fine. But do not replace with Try<Void, X>, otherwise, or() or
       * orMapCause() can attempt to produce Void instances.
       */
      @SuppressWarnings("unchecked")
      final Try<U, X> casted = (Try<U, X>) this;
      return casted;
    }

    @Override
    public Try<Object, X> andRun(TRunnable<? extends X> runnable) {
      return this;
    }

    @Override
    public Try<Object, X> andConsume(TConsumer<? super Object, ? extends X> consumer) {
      return this;
    }

    @Override
    public <U, V, Y extends Exception> Try<V, X> and(Try<U, ? extends X> t2,
        TBiFunction<? super Object, ? super U, ? extends V, Y> merger) throws Y {
      return cast();
    }

    @Override
    public <U> Try<U, X>
        andApply(TFunction<? super Object, ? extends U, ? extends X> mapper) {
      return cast();
    }

    @Override
    public <Y extends Exception, Z extends Exception, W extends Exception> Try<Object, Z> or(
        TSupplier<? extends Object, Y> supplier,
        TBiFunction<? super X, ? super Y, ? extends Z, W> exceptionsMerger) throws W {
      final Try<Object, Y> t2 = Try.get(supplier);
      return t2.map(Try::success, y -> Try.failure(exceptionsMerger.apply(cause, y)));
    }
  }

  public static class TryVoidSuccess extends TryVariableCatchVoidSuccess<Exception, Exception>
      implements TryVoid<Exception> {
    public static <X extends Exception> TryVoid<X> given() {
      return new TryVoidSuccess().cast();
    }

    private TryVoidSuccess() {
      /* Reducing visibility. */
    }

    private <Y extends Exception> TryVoid<Y> cast() {
      /*
       * Safe: there is no cause in this (immutable) instance, thus its declared type does not
       * matter.
       */
      @SuppressWarnings("unchecked")
      final TryVoid<Y> casted = (TryVoid<Y>) this;
      return casted;
    }

    @Override
    public <T> Try<T, Exception>
        andGet(TSupplier<? extends T, ? extends Exception> supplier) {
      return Try.get(supplier);
    }

    @Override
    public TryVoid<Exception> andRun(TRunnable<? extends Exception> runnable) {
      return TryVoid.run(runnable);
    }

    @Override
    public TryVoid<Exception> or(TRunnable<? extends Exception> runnable) {
      return this;
    }
  }

  public static class TryVoidFailure<X extends Exception>
      extends TryVariableCatchVoidFailure<X, Exception> implements TryVoid<X> {
    public static <X extends Exception> TryVoid<X> given(X cause) {
      return new TryVoidFailure<>(cause);
    }

    private TryVoidFailure(X cause) {
      super(cause);
    }

    @Override
    public <T> Try<T, X> andGet(TSupplier<? extends T, ? extends X> supplier) {
      return Try.failure(cause);
    }

    @Override
    public TryVoid<X> andRun(TRunnable<? extends X> runnable) {
      return this;
    }

    @Override
    public TryVoid<X> or(TRunnable<? extends X> runnable) {
      return TryVoid.run(runnable);
    }
  }

  public static class TryCatchAllSuccess<T> extends
      TryOptionalImpl.TryVariableCatchSuccess<T, Throwable, Throwable> implements TryCatchAll<T> {
    public static <T> TryCatchAll<T> given(T result) {
      return new TryCatchAllSuccess<>(result);
    }

    private TryCatchAllSuccess(T result) {
      super(result);
    }

    @Override
    public <W extends Exception> TryCatchAll<T> or(TSupplier<? extends T, ?> supplier,
        TBiFunction<? super Throwable, ? super Throwable, ? extends Throwable,
            W> exceptionsMerger)
        throws W {
      return this;
    }

    @Override
    public TryCatchAll<T> andRun(TRunnable<?> runnable) {
      final TryCatchAllVoid ran = TryCatchAllVoid.run(runnable);
      return ran.map(() -> this, TryCatchAll::failure);
    }

    @Override
    public TryCatchAll<T> andConsume(TConsumer<? super T, ?> consumer) {
      return andRun(() -> consumer.accept(result));
    }

    @Override
    public <U, V, Y extends Exception> TryCatchAll<V> and(TryCatchAll<U> t2,
        TBiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y {
      return t2.map(u -> TryCatchAll.success(merger.apply(result, u)), TryCatchAll::failure);
    }

    @Override
    public <U> TryCatchAll<U> andApply(TFunction<? super T, ? extends U, ?> mapper) {
      return TryCatchAll.get(() -> mapper.apply(result));
    }
  }

  public static class TryCatchAllFailure extends
      TryOptionalImpl.TryVariableCatchFailure<Throwable, Throwable> implements TryCatchAll<Object> {
    public static <T> TryCatchAll<T> given(Throwable cause) {
      return new TryCatchAllFailure(cause).cast();
    }

    private TryCatchAllFailure(Throwable cause) {
      super(cause);
    }

    private <U> TryCatchAll<U> cast() {
      @SuppressWarnings("unchecked")
      final TryCatchAll<U> casted = (TryCatchAll<U>) this;
      return casted;
    }

    @Override
    public <W extends Exception> TryCatchAll<Object>
        or(TSupplier<? extends Object, ?> supplier, TBiFunction<? super Throwable,
            ? super Throwable, ? extends Throwable, W> exceptionsMerger) throws W {
      final TryCatchAll<Object> t2 = TryCatchAll.get(supplier);
      return t2.map(TryCatchAll::success, y -> {
        final Throwable merged = exceptionsMerger.apply(cause, y);
        checkNotNull(merged, "Exceptions merger returned null");
        return TryCatchAll.failure(merged);
      });
    }

    @Override
    public TryCatchAll<Object> andRun(TRunnable<?> runnable) {
      return this;
    }

    @Override
    public TryCatchAll<Object> andConsume(TConsumer<? super Object, ?> consumer) {
      return this;
    }

    @Override
    public <U, V, Y extends Exception> TryCatchAll<V> and(TryCatchAll<U> t2,
        TBiFunction<? super Object, ? super U, ? extends V, Y> merger) throws Y {
      return cast();
    }

    @Override
    public <U> TryCatchAll<U> andApply(TFunction<? super Object, ? extends U, ?> mapper) {
      return cast();
    }
  }

  public static class TryCatchAllVoidSuccess extends
      TryOptionalImpl.TryVariableCatchVoidSuccess<Throwable, Throwable> implements TryCatchAllVoid {
    public static TryCatchAllVoid given() {
      return new TryCatchAllVoidSuccess();
    }

    private TryCatchAllVoidSuccess() {
      /* Reducing visibility. */
    }

    @Override
    public <T> TryCatchAll<T> andGet(TSupplier<? extends T, ?> supplier) {
      return TryCatchAll.get(supplier);
    }

    @Override
    public TryCatchAllVoid andRun(TRunnable<?> runnable) {
      return TryCatchAllVoid.run(runnable);
    }

    @Override
    public TryCatchAllVoid or(TRunnable<?> runnable) {
      return this;
    }
  }

  public static class TryCatchAllVoidFailure extends
      TryOptionalImpl.TryVariableCatchVoidFailure<Throwable, Throwable> implements TryCatchAllVoid {
    public static TryCatchAllVoid given(Throwable cause) {
      return new TryCatchAllVoidFailure(cause);
    }

    private TryCatchAllVoidFailure(Throwable cause) {
      super(cause);
    }

    @Override
    public <T> TryCatchAll<T> andGet(TSupplier<? extends T, ?> supplier) {
      return TryCatchAll.failure(cause);
    }

    @Override
    public TryCatchAllVoid andRun(TRunnable<?> runnable) {
      return this;
    }

    @Override
    public TryCatchAllVoid or(TRunnable<?> runnable) {
      return TryCatchAllVoid.run(runnable);
    }
  }

  protected TryOptionalImpl() {
    /* Reducing visibility. */
  }

  @Override
  public boolean isSuccess() {
    return getCause().isEmpty();
  }

  @Override
  public boolean isFailure() {
    return getCause().isPresent();
  }

  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof TryOptional)) {
      return false;
    }

    final TryOptional<?, ?> t2 = (TryOptional<?, ?>) o2;
    return getResult().equals(t2.getResult()) && getCause().equals(t2.getCause());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getResult(), getCause());
  }
}
