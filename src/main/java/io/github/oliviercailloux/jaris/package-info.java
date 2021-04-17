/**
 * A collection of various helpers and types that are felt to miss from the JDK, with a focus on
 * best practices and well-designed API, similar to
 * <a href="https://github.com/google/guava">Guava</a> in spirit.
 * <p>
 * Every methods in this library are
 * <a href="https://github.com/google/guava/wiki/UsingAndAvoidingNullExplained">null hostile</a>:
 * they throw {@link java.lang.NullPointerException} if given {@code null} arguments, and never
 * return {@code null} values.
 * <p>
 * This library does not sneaky-throw.
 */
package io.github.oliviercailloux.jaris;
