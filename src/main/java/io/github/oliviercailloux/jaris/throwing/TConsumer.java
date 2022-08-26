package io.github.oliviercailloux.jaris.throwing;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generalization of {@link java.util.function.Consumer} that may throw instances of type {@code X},
 * not just {@code RuntimeException} instances.
 *
 * @param <T> the type of the input to the operation
 * @param <X> a sort of throwable that this consumer may throw
 */
@FunctionalInterface
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface TConsumer<T, X extends Throwable> {
  /**
   * Performs this operation on the given argument.
   *
   * @param t the input argument
   * @throws X in generally unspecified circumstances
   */
  public void accept(T t) throws X;

  /**
   * Returns a composed {@code Throwing.Consumer} that performs, in sequence, this operation
   * followed by the {@code after} operation.
   *
   * @param after the operation to perform after this operation
   * @return a composed {@code Throwing.Consumer} that performs in sequence this operation followed
   *         by the {@code after} operation
   * @see java.util.function.Consumer#andThen(java.util.function.Consumer)
   */
  default TConsumer<T, X> andThen(TConsumer<? super T, ? extends X> after) {
    checkNotNull(after);
    return t -> {
      accept(t);
      after.accept(t);
    };
  }
}
