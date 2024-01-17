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
  public interface TryVariableCatchInterface<T, X extends Z, Z extends Throwable> {

    /**
     * Returns {@code true} iff this instance contains a result (and not a cause).
     *
     * @return {@code true} iff {@link #isFailure()} returns {@code false}
     */
    public boolean isSuccess();

    /**
     * Return {@code true} iff this instance contains a cause (and not a result).
     *
     * @return {@code true} iff {@link #isSuccess()} returns {@code false}
     */
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
    public <Y extends Exception> Optional<T> orConsumeCause(TConsumer<? super X, Y> consumer)
        throws Y;

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
