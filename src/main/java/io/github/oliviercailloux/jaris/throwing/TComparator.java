package io.github.oliviercailloux.jaris.throwing;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generalization of {@link java.util.Comparator} that may throw instances of type {@code X}, not
 * just {@code RuntimeException} instances.
 *
 * @param <T> the type of objects that may be compared by this comparator
 * @param <X> a sort of throwable that this comparator may throw
 */
@FunctionalInterface
public interface TComparator<T, X extends Throwable> {
  /**
   * Accepts a function that extracts a sort key from a type {@code T}, and returns a
   * {@code Throwing.Comparator<T>} that compares by that sort key using the specified
   * {@link TComparator}.
   *
   * @param <T> the type of element to be compared
   * @param <U> the type of the sort key
   * @param <X> a sort of throwable that the returned comparator may throw
   * @param keyExtractor the function used to extract the sort key
   * @param keyComparator the {@code Throwing.Comparator} used to compare the sort key
   * @return a comparator that compares by an extracted key using the specified
   *         {@code Throwing.Comparator}
   * @see java.util.Comparator#comparing(java.util.TFunction, java.util.Comparator)
   */
  public static <T, U, X extends Throwable> TComparator<T, X> comparing(
      TFunction<? super T, ? extends U, ? extends X> keyExtractor,
      TComparator<? super U, ? extends X> keyComparator) {
    checkNotNull(keyExtractor);
    checkNotNull(keyComparator);
    return (c1, c2) -> keyComparator.compare(keyExtractor.apply(c1), keyExtractor.apply(c2));
  }

  /**
   * Accepts a function that extracts a {@link Comparable} sort key from a type {@code T}, and
   * returns a {@code Throwing.Comparator<T>} that compares by that sort key.
   *
   * @param <T> the type of element to be compared
   * @param <U> the type of the {@code Comparable} sort key
   * @param <X> a sort of throwable that the returned comparator may throw
   * @param keyExtractor the function used to extract the {@link Comparable} sort key
   * @return a comparator that compares by an extracted key
   */
  public static <T, U extends Comparable<? super U>, X extends Throwable> TComparator<T, X>
      comparing(TFunction<? super T, ? extends U, ? extends X> keyExtractor) {
    checkNotNull(keyExtractor);
    return (c1, c2) -> keyExtractor.apply(c1).compareTo(keyExtractor.apply(c2));
  }

  /**
   * Compares its two arguments for order.
   *
   * @param o1 the first object to be compared.
   * @param o2 the second object to be compared.
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
   *         equal to, or greater than the second.
   * @throws NullPointerException if an argument is null and this comparator does not permit null
   *         arguments
   * @throws ClassCastException if the arguments' types prevent them from being compared by this
   *         comparator.
   * @throws X in generally unspecified circumstances
   * @see java.util.Comparator#compare(Object, Object) <code>Comparator.compare(T, T)</code>
   */
  public int compare(T o1, T o2) throws X;

  /**
   * Indicates whether some other object is “equal” to this comparator, implying that the other
   * object is also a {@code Throwing.Comparator} and imposes the same ordering.
   *
   * @param obj the reference object with which to compare.
   * @return {@code true} only if the specified object is also a {@code Throwing.Comparator} and it
   *         imposes the same ordering as this comparator.
   * @see java.util.Comparator#equals(Object)
   */
  @Override
  boolean equals(Object obj);

  /**
   * Returns a comparator that imposes the reverse ordering of this comparator.
   *
   * @return a comparator that imposes the reverse ordering of this comparator.
   * @see java.util.Comparator#reversed()
   */
  default TComparator<T, X> reversed() {
    return (o1, o2) -> compare(o2, o1);
  }

  /**
   * Returns a lexicographic-order comparator with another comparator.
   *
   * @param other the other comparator to be used when this comparator compares two objects that are
   *        equal.
   * @return a lexicographic-order comparator composed of this and then the other comparator
   * @see java.util.Comparator#thenComparing(java.util.Comparator)
   */
  default TComparator<T, X> thenComparing(TComparator<? super T, ? extends X> other) {
    checkNotNull(other);
    return (c1, c2) -> {
      int res = compare(c1, c2);
      return (res != 0) ? res : other.compare(c1, c2);
    };
  }

  /**
   * Returns a lexicographic-order comparator with a function that extracts a key to be compared
   * with the given {@code Throwing.Comparator}.
   *
   * @param <U> the type of the sort key
   * @param keyExtractor the function used to extract the sort key
   * @param keyComparator the {@code Throwing.Comparator} used to compare the sort key
   * @return a lexicographic-order comparator composed of this comparator and then comparing on the
   *         key extracted by the keyExtractor function
   * @see #thenComparing(Throwing.TComparator)
   * @see java.util.Comparator#thenComparing(java.util.TFunction, java.util.Comparator)
   */
  default <U> TComparator<T, X> thenComparing(
      TFunction<? super T, ? extends U, ? extends X> keyExtractor,
      TComparator<? super U, ? extends X> keyComparator) {
    return thenComparing(comparing(keyExtractor, keyComparator));
  }

  /**
   * Returns a lexicographic-order comparator with a function that extracts a {@code Comparable}
   * sort key.
   *
   * @param <U> the type of the {@link Comparable} sort key
   * @param keyExtractor the function used to extract the {@link Comparable} sort key
   * @return a lexicographic-order comparator composed of this and then the {@link Comparable} sort
   *         key.
   * @see #thenComparing(Throwing.TComparator)
   * @see java.util.Comparator#thenComparing(java.util.TFunction)
   */
  default <U extends Comparable<? super U>> TComparator<T, X>
      thenComparing(TFunction<? super T, ? extends U, ? extends X> keyExtractor) {
    return thenComparing(comparing(keyExtractor));
  }
}
