/**
 * Variations on the standard functional interfaces which can declare throwing throwables.
 * <p>
 * To be used instead of the standard equivalent when (some of) the methods may throw throwables
 * that are not {@link RuntimeException} instances.
 * </p>
 * <p>
 * In all these interfaces, {@code X} designates a type of (typically, checked) exception that (some
 * of) the methods may throw. These methods may throw other throwable instances, but the
 * <em>only</em> sort of <em>checked</em> exception it can throw should be of type {@code X}. (By
 * the rules of Java, this will be guaranteed by the compiler, unless sneaky-throw is used.)
 * <p>
 * In typical usage of these interfaces, the type parameter {@code X} is a <em>checked
 * exception</em>. Other uses are also possible, as follows.
 * </p>
 * <ul>
 * <li>Binding {@code X} to {@code RuntimeException} (or a child thereof) may be useful for falling
 * back to a behavior that does not treat exceptions specially. For example, when a
 * {@link io.github.oliviercailloux.jaris.exceptions.CheckedStream} is used with a throwing
 * interface that declare a {@code RuntimeException}, the {@code CheckedStream} behaves like a
 * {@code Stream}.</li>
 * <li>The signatures also allow for {@code X} to extend merely {@link Throwable}, which can be
 * useful in very specific circumstances (for a library that checks that some code does not throw,
 * for example; or for {@link io.github.oliviercailloux.jaris.exceptions.TryCatchAll#orThrow()} to
 * convert a try safe into a supplier). It is strongly recommended however to consider restricting
 * {@code X} to extend {@code Exception} at the usage site. For example,
 * {@link io.github.oliviercailloux.jaris.exceptions.Try} implements such a restriction. That is
 * because throwables that are not exceptions should generally not be caught.</li>
 * </ul>
 * <p>
 * Inspired from the <a href=
 * "https://github.com/diffplug/durian/blob/99100976d27a5ebec74a0a7df48fc23de822fa00/src/com/diffplug/common/base/Throwing.java">durian</a>
 * library; simplified.
 * </p>
 * <p>
 * The naming (TFunction, etc.) is chosen so as to not shadow the JDK types: name shadowing of such
 * common types, especially of types in {@code java.lang} such as {@link java.lang.Runnable}, could
 * render user code more obscure.
 * </p>
 */
package io.github.oliviercailloux.jaris.throwing;
