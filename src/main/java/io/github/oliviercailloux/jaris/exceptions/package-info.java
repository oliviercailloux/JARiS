/**
 * Objects to deal with code that declare checked exceptions; or that throw exceptions.
 * <p>
 * In the {@code Try*} hierarchy, when a function or a supplier given as parameter of some method
 * returns {@code null}, it is treated as if it had thrown a {@link java.lang.NullPointerException}
 * instead. This permits to guarantee that a try does not contain a {@code null} result or cause.
 */
package io.github.oliviercailloux.jaris.exceptions;
