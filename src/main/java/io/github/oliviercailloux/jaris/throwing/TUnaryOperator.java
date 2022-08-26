package io.github.oliviercailloux.jaris.throwing;

/**
 * Generalization of {@link java.util.function.UnaryOperator} that may throw instances of type
 * {@code X}, not just {@code RuntimeException} instances.
 *
 * @param <T> the type of the operand and result of the operator
 * @param <X> a sort of throwable that the {@code Throwing.UnaryOperator} may throw
 */
@FunctionalInterface
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface TUnaryOperator<T, X extends Throwable> extends TFunction<T, T, X> {

}
