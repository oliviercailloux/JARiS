package io.github.oliviercailloux.jaris.throwing;

/**
 * Generalization of {@link java.util.function.Supplier} that may throw instances of type {@code X},
 * not just {@code RuntimeException} instances.
 *
 * @param <T> the type of results supplied by this supplier
 * @param <X> a sort of throwable that this supplier may throw
 */
@FunctionalInterface
public interface TSupplier<T, X extends Throwable> {
  /**
   * Gets a result.
   *
   * @return a result.
   * @throws X in generally unspecified circumstances
   */
  public T get() throws X;
}
