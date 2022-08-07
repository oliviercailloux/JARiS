package io.github.oliviercailloux.jaris.throwing;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generalization of {@link java.util.function.BiConsumer} that may throw instances of type
 * {@code X}, not just {@code RuntimeException} instances.
 *
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 * @param <X> a sort of throwable that the {@code Throwing.BiConsumer} may throw
 */
@FunctionalInterface
public interface TBiConsumer<T, U, X extends Throwable> {
  /**
   * Performs this operation on the given arguments.
   *
   * @param t the first input argument
   * @param u the second input argument
   * @throws X in generally unspecified circumstances
   */
  public void accept(T t, U u) throws X;

  /**
   * Returns a composed {@code Throwing.BiConsumer} that performs, in sequence, this operation
   * followed by the {@code after} operation.
   *
   * @param after the operation to perform after this operation
   * @return a composed {@code Throwing.BiConsumer} that performs in sequence this operation
   *         followed by the {@code after} operation
   * @see java.util.function.BiConsumer#andThen(java.util.function.BiConsumer)
   */
  default TBiConsumer<T, U, X> andThen(TBiConsumer<? super T, ? super U, ? extends X> after) {
    checkNotNull(after);

    return (l, r) -> {
      accept(l, r);
      after.accept(l, r);
    };
  }
}
