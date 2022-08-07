package io.github.oliviercailloux.jaris.throwing;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generalization of {@link java.util.function.BiPredicate} that may throw instances of type
 * {@code X}, not just {@code RuntimeException} instances.
 *
 * @param <T> the type of the first argument to the predicate
 * @param <U> the type of the second argument the predicate
 * @param <X> a sort of throwable that the {@code Throwing.BiPredicate} may throw
 */
@FunctionalInterface
public interface TBiPredicate<T, U, X extends Throwable> {
  /**
   * Evaluates this predicate on the given arguments.
   *
   * @param t the first input argument
   * @param u the second input argument
   * @return {@code true} if the input arguments match the predicate, otherwise {@code false}
   * @throws X in generally unspecified circumstances
   */
  public boolean test(T t, U u) throws X;

  /**
   * Returns a composed predicate that represents a short-circuiting logical AND of this predicate
   * and another.
   *
   * @param other a predicate that will be logically-ANDed with this predicate
   * @return a composed predicate that represents the short-circuiting logical AND of this predicate
   *         and the {@code other} predicate
   * @see java.util.function.BiPredicate#and(java.util.function.BiPredicate)
   */
  default TBiPredicate<T, U, X> and(TBiPredicate<? super T, ? super U, ? extends X> other) {
    checkNotNull(other);
    return (T t, U u) -> test(t, u) && other.test(t, u);
  }

  /**
   * Returns a composed predicate that represents a short-circuiting logical OR of this predicate
   * and another.
   *
   * @param other a predicate that will be logically-ORed with this predicate
   * @return a composed predicate that represents the short-circuiting logical OR of this predicate
   *         and the {@code other} predicate
   * @see java.util.function.BiPredicate#or(java.util.function.BiPredicate)
   */
  default TBiPredicate<T, U, X> or(TBiPredicate<? super T, ? super U, ? extends X> other) {
    checkNotNull(other);
    return (T t, U u) -> test(t, u) || other.test(t, u);
  }

  /**
   * Returns a predicate that represents the logical negation of this predicate.
   *
   * @return a predicate that represents the logical negation of this predicate
   */
  default TBiPredicate<T, U, X> negate() {
    return (T t, U u) -> !test(t, u);
  }
}
