package io.github.oliviercailloux.jaris.throwing;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generalization of {@link java.util.function.BiFunction} that may throw instances of type
 * {@code X}, not just {@code RuntimeException} instances.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of the result of the function
 * @param <X> a sort of throwable that the {@code Throwing.Function} may throw
 */
@FunctionalInterface
public interface TBiFunction<T, U, R, X extends Throwable> {
  /**
   * Applies this function to the given arguments.
   *
   * @param t the first function argument
   * @param u the second function argument
   * @return the function result
   * @throws X in generally unspecified circumstances
   */
  public R apply(T t, U u) throws X;

  /**
   * Returns a composed function that first applies this function to its input, and then applies the
   * {@code after} function to the result.
   *
   * @param <V> the type of output of the {@code after} function, and of the composed function
   * @param after the function to apply after this function is applied
   * @return a composed function that first applies this function and then applies the {@code after}
   *         function
   * @see java.util.function.BiFunction#andThen(java.util.function.Function)
   */
  default <V> TBiFunction<T, U, V, X>
      andThen(TFunction<? super R, ? extends V, ? extends X> after) {
    checkNotNull(after);
    return (t, u) -> after.apply(apply(t, u));
  }
}
