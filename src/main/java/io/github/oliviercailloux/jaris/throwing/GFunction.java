package io.github.oliviercailloux.jaris.throwing;

import java.util.function.Function;

/**
 * Beta. Let’s see if this is useful.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
interface GFunction<T, R> extends Function<T, R>, TFunction<T, R, RuntimeException> {
}
