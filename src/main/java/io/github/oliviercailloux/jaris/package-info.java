/**
 * A collection of various helpers and types that are felt to miss from the JDK, with a focus on
 * best practices and well-designed API, similar to
 * <a href="https://github.com/google/guava">Guava</a> in spirit. See also the
 * <a href="https://github.com/oliviercailloux/JARiS/blob/master/README.adoc">readme</a> of this
 * library.
 *
 * <p>
 * Every methods in this library are
 * <a href="https://github.com/google/guava/wiki/UsingAndAvoidingNullExplained">null hostile</a>:
 * they throw {@link java.lang.NullPointerException} if given some {@code null} argument, and never
 * return {@code null} values.
 * </p>
 *
 * <p>
 * This library does not <a href=
 * "https://objectcomputing.com/resources/publications/sett/january-2010-reducing-boilerplate-code-with-project-lombok#sneaky">sneaky-throw</a>.
 * </p>
 *
 * <p>
 * This library makes no guarantee about serializability.
 * </p>
 *
 * <p>
 * Please <a href="https://www.github.com/oliviercailloux/JARiS/issues">open issues</a> if you find
 * bugs or have feature requests. I will consider the bug reports seriously.
 * </p>
 */
package io.github.oliviercailloux.jaris;
