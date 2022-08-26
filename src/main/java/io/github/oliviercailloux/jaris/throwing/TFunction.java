package io.github.oliviercailloux.jaris.throwing;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generalization of {@link java.util.function.Function} that may throw instances of type {@code X},
 * not just {@code RuntimeException} instances.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <X> a sort of throwable that the function may throw
 */
@FunctionalInterface
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface TFunction<T, R, X extends Throwable> {
  /**
   * Applies this function to the given argument.
   *
   * @param t the function argument
   * @return the function result
   * @throws X in generally unspecified circumstances
   */
  public R apply(T t) throws X;

  /**
   * Returns a composed function that first applies the {@code before} function to its input, and
   * then applies this function to the result.
   *
   * @param <V> the type of input to the {@code before} function, and to the composed function
   * @param before the function to apply before this function is applied
   * @return a composed function that first applies the {@code before} function and then applies
   *         this function
   *
   * @see #andThen(Throwing.TFunction)
   * @see java.util.function.Function#compose(java.util.function.Function)
   */
  default <V> TFunction<V, R, X> compose(TFunction<? super V, ? extends T, ? extends X> before) {
    checkNotNull(before);
    return v -> apply(before.apply(v));
  }

  /**
   * Returns a composed function that first applies this function to its input, and then applies the
   * {@code after} function to the result.
   *
   * @param <V> the type of output of the {@code after} function, and of the composed function
   * @param after the function to apply after this function is applied
   * @return a composed function that first applies this function and then applies the {@code after}
   *         function
   *
   * @see #compose(TFunction)
   * @see java.util.function.Function#andThen(java.util.function.Function)
   */
  default <V> TFunction<T, V, X> andThen(TFunction<? super R, ? extends V, ? extends X> after) {
    checkNotNull(after);
    return t -> after.apply(apply(t));
  }
}
