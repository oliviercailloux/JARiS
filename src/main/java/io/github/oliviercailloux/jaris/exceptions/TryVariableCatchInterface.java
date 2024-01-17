package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.throwing.TConsumer;
import io.github.oliviercailloux.jaris.throwing.TFunction;
import io.github.oliviercailloux.jaris.throwing.TRunnable;
import java.util.Optional;
import java.util.function.Function;

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

}