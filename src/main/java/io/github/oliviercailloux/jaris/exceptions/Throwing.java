package io.github.oliviercailloux.jaris.exceptions;

import java.util.stream.Stream;

/**
 * <p>
 * Variations on the standard functional interfaces which can declare a checked exception. To be
 * used instead of the standard equivalent when (some of) the methods declare a checked exception.
 * <p>
 * Note that a typical usage of these interfaces will bind a <em>checked exception</em> to the type
 * parameter {@code X}, though contrary usage (when {@code X} is bound to {@link RuntimeException}
 * or a child thereof) may be useful for falling back to a behavior that does not treat exceptions
 * specially. For example, when a {@link CheckedStream} is used with a throwing interface that
 * declare a {@link RuntimeException}, the {@link CheckedStream} behaves like a {@link Stream}.
 * <p>
 * In all these interfaces, {@code X} designates a type of (typically, checked) exception that (some
 * of) the methods may throw. These methods may throw other throwable instances, but the
 * <em>only</em> sort of <em>checked</em> exception it can throw should be of type <code>X</code>.
 * By the rules of Java, this will be guaranteed by the compiler, unless sneaky-throw is used.
 * <p>
 * This library assumes no sneaky-throw is used, otherwise, the behavior of any method related to
 * the throwing functional interfaces is unspecified.
 * <p>
 * TODO add default methods.
 * <p>
 * Inspired from the <a href=
 * "https://github.com/diffplug/durian/blob/99100976d27a5ebec74a0a7df48fc23de822fa00/src/com/diffplug/common/base/Throwing.java">durian</a>
 * library; simplified.
 * <p>
 * The signatures allow for {@code X} to extend merely {@link Throwable}, which can be useful in
 * very specific circumstances. It is strongly recommended however to consider restricting {@code X}
 * to extend Exception at the call site: throwables that are not exceptions should generally not be
 * caught.
 */
public final class Throwing {


  /**
   * I initially thought about allowing only {@code X} to extend Exception. But this creates a
   * practical problem. Using TrySafe::orThrow to convert a try safe into a supplier requires
   * {@code X} to be allowed to extend Throwable. Besides, it now seems to me that the constraint of
   * Exception extension is unduly restrictive: such restrictions should be enforced at the
   * reception site, where developer knows what versions make sense. Someone may wish to write a
   * library that checks that some code does not throw, for example, in which case the larger
   * (unconstrained) version would be useful.
   */
  private Throwing() {
    /* Not for instanciation. */
  }

  /**
   * Equivalent of {@link java.lang.Runnable} that may declare a checked exception.
   *
   * @param <X> a sort of exception that this runnable may throw
   */
  @FunctionalInterface
  public interface Runnable<X extends Throwable> {
    /**
     * Takes an action.
     *
     * @throws X unspecified
     * @see java.lang.Runnable#run()
     */
    public void run() throws X;
  }

  /**
   * Equivalent of {@link java.util.function.Supplier} that may declare a checked exception.
   *
   * @param <X> a sort of exception that this supplier may throw
   */
  @FunctionalInterface
  public interface Supplier<T, X extends Throwable> {
    /**
     * Gets a result.
     *
     * @return a result.
     * @throws X unspecified
     */
    public T get() throws X;
  }

  /**
   * Equivalent of {@link java.util.Comparator} that may declare a checked exception.
   * <p>
   * TODO add default methods.
   *
   * @param <X> a sort of exception that this comparator may throw
   */
  @FunctionalInterface
  public interface Comparator<T, X extends Throwable> {
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
     * @throws X unspecified
     * @see java.util.Comparator#compare(Object, Object)
     */
    public int compare(T o1, T o2) throws X;
  }

  @FunctionalInterface
  public interface Consumer<T, X extends Throwable> {
    public void accept(T t) throws X;
  }

  @FunctionalInterface
  public interface Function<T, R, X extends Throwable> {
    public R apply(T t) throws X;
  }

  @FunctionalInterface
  public interface Predicate<T, X extends Throwable> {
    public boolean test(T t) throws X;

    public default <Y extends X> Predicate<T, X> and(Predicate<? super T, Y> p2) {
      return t -> test(t) && p2.test(t);
    }

    public default <Y extends X> Predicate<T, X> or(Predicate<? super T, Y> p2) {
      return t -> test(t) || p2.test(t);
    }

    public default Predicate<T, X> negate() {
      return t -> !test(t);
    }
  }

  @FunctionalInterface
  public interface BiConsumer<T, U, X extends Throwable> {
    public void accept(T t, U u) throws X;
  }

  @FunctionalInterface
  public interface BiFunction<T, U, R, X extends Throwable> {
    public R apply(T t, U u) throws X;
  }

  @FunctionalInterface
  public interface BiPredicate<T, U, X extends Throwable> {
    public boolean accept(T t, U u) throws X;
  }
}
