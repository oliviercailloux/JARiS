package io.github.oliviercailloux.jaris.throwing;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generalization of {@link java.util.function.BinaryOperator} that may throw instances of type
 * {@code X}, not just {@code RuntimeException} instances.
 *
 * @param <T> the type of the operands and result of the operator
 * @param <X> a sort of throwable that the {@code Throwing.BinaryOperator} may throw
 */
@FunctionalInterface
public interface TBinaryOperator<T, X extends Throwable> extends TBiFunction<T, T, T, X> {
  /**
   * Returns a {@link TBinaryOperator} which returns the lesser of two elements according to the
   * specified {@code Comparator}.
   *
   * @param <T> the type of the input arguments of the comparator
   * @param <X> the sort of throwable that the returned instance may throw
   * @param comparator a {@code Throwing.Comparator} for comparing the two values
   * @return a {@code Throwing.BinaryOperator} which returns the lesser of its operands, according
   *         to the supplied {@code Throwing.Comparator}
   */
  public static <T, X extends Throwable> TBinaryOperator<T, X>
      minBy(TComparator<? super T, ? extends X> comparator) {
    checkNotNull(comparator);
    return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
  }

  /**
   * Returns a {@link TBinaryOperator} which returns the greater of two elements according to the
   * specified {@code Comparator}.
   *
   * @param <T> the type of the input arguments of the comparator
   * @param <X> the sort of throwable that the returned instance may throw
   * @param comparator a {@code Throwing.Comparator} for comparing the two values
   * @return a {@code Throwing.BinaryOperator} which returns the greater of its operands, according
   *         to the supplied {@code Throwing.Comparator}
   */
  public static <T, X extends Throwable> TBinaryOperator<T, X>
      maxBy(TComparator<? super T, ? extends X> comparator) {
    checkNotNull(comparator);
    return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
  }
}
