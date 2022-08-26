package io.github.oliviercailloux.jaris.throwing;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generalization of {@link java.util.function.Predicate} that may throw instances of type
 * {@code X}, not just {@code RuntimeException} instances.
 *
 * @param <T> the type of the input to the predicate
 * @param <X> a sort of throwable that the predicate may throw
 */
@FunctionalInterface
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface TPredicate<T, X extends Throwable> {
  /**
   * Returns a throwing predicate that is the negation of the supplied predicate.
   *
   * @param <T> the type of arguments to the specified predicate
   * @param <X> the sort of throwable that the returned instance may throw
   * @param target predicate to negate
   * @return a predicate that negates the results of the supplied predicate
   *
   */
  @SuppressWarnings("unchecked")
  static <T, X extends Throwable> TPredicate<T, X> not(TPredicate<? super T, ? extends X> target) {
    checkNotNull(target);
    return (TPredicate<T, X>) target.negate();
  }

  /**
   * Evaluates this predicate on the given argument.
   *
   * @param t the input argument
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   * @throws X in generally unspecified circumstances
   */
  public boolean test(T t) throws X;

  /**
   * Returns a composed predicate that represents a short-circuiting logical AND of this predicate
   * and another.
   *
   * @param p2 a predicate that will be logically-ANDed with this predicate
   * @return a composed predicate that represents the short-circuiting logical AND of this predicate
   *         and the {@code other} predicate
   * @see java.util.function.Predicate#and(java.util.function.Predicate)
   */
  public default TPredicate<T, X> and(TPredicate<? super T, ? extends X> p2) {
    return t -> test(t) && p2.test(t);
  }

  /**
   * Returns a composed predicate that represents a short-circuiting logical OR of this predicate
   * and another.
   *
   * @param p2 a predicate that will be logically-ORed with this predicate
   * @return a composed predicate that represents the short-circuiting logical OR of this predicate
   *         and the {@code other} predicate
   * @see java.util.function.Predicate#or(java.util.function.Predicate)
   */
  public default TPredicate<T, X> or(TPredicate<? super T, ? extends X> p2) {
    return t -> test(t) || p2.test(t);
  }

  /**
   * Returns a predicate that represents the logical negation of this predicate.
   *
   * @return a predicate that represents the logical negation of this predicate
   */
  public default TPredicate<T, X> negate() {
    return t -> !test(t);
  }
}
