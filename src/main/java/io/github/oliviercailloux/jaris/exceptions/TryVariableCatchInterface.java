package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.throwing.TConsumer;
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
   * Runs the consumer iff this instance is a success, and returns this instance if it succeeds
   * and the cause of failure if it throws a catchable throwable; otherwise, returns this
   * instance.
   *
   * @param consumer the consumer to invoke iff this instance is a success
   * @return a success iff this instance is a success and the provided consumer does not throw
   */
  public TryVariableCatchInterface<T, ?, Z>
      andConsume(TConsumer<? super T, ? extends X> consumer);

}