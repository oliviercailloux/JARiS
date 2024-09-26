package io.github.oliviercailloux.jaris.throwing;

/**
 * Generalization of {@link java.util.stream.Collector} that may throw instances of type
 * {@code X}, not just {@code RuntimeException} instances.
 *
 * @param <T> the type of input elements to the reduction operation
 * @param <A> the mutable accumulation type of the reduction operation (often
 *            hidden as an implementation detail)
 * @param <R> the result type of the reduction operation
 * @param <X> a sort of throwable that the {@code Throwing.BiConsumer} may throw
 */
@FunctionalInterface
public class TCollector<T, A, R, X extends Throwable> {
  
}
