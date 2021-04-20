package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Variations on the standard functional interfaces which can declare throwing throwables.
 * <p>
 * To be used instead of the standard equivalent when (some of) the methods may throw throwables
 * that are not {@link RuntimeException} instances.
 * </p>
 * <p>
 * In all these interfaces, {@code X} designates a type of (typically, checked) exception that (some
 * of) the methods may throw. These methods may throw other throwable instances, but the
 * <em>only</em> sort of <em>checked</em> exception it can throw should be of type {@code X}. By the
 * rules of Java, this will be guaranteed by the compiler, unless sneaky-throw is used.
 * <p>
 * In typical usage of these interfaces, the type parameter {@code X} is a <em>checked
 * exception</em>. Other uses are also possible, as follows.
 * </p>
 * <ul>
 * <li>Binding {@code X} to {@code RuntimeException} (or a child thereof) may be useful for falling
 * back to a behavior that does not treat exceptions specially. For example, when a
 * {@link CheckedStream} is used with a throwing interface that declare a {@code RuntimeException},
 * the {@code CheckedStream} behaves like a {@code Stream}.</li>
 * <li>The signatures also allow for {@code X} to extend merely {@link Throwable}, which can be
 * useful in very specific circumstances. It is strongly recommended however to consider restricting
 * {@code X} to extend {@code Exception} at the usage site. For example, {@link Try} implements such
 * a restriction. That is because throwables that are not exceptions should generally not be caught.
 * </li>
 * </ul>
 * <p>
 * Inspired from the <a href=
 * "https://github.com/diffplug/durian/blob/99100976d27a5ebec74a0a7df48fc23de822fa00/src/com/diffplug/common/base/Throwing.java">durian</a>
 * library; simplified.
 * </p>
 */
public final class Throwing {

  /**
   * I initially thought about allowing only {@code X} to extend Exception. But this creates a
   * practical problem. Using {@link TryCatchAll#orThrow()} to convert a try safe into a supplier
   * requires {@code X} to be allowed to extend Throwable. Besides, it now seems to me that the
   * constraint of Exception extension is unduly restrictive: such restrictions should be enforced
   * at the reception site, where developer knows what versions make sense. Someone may wish to
   * write a library that checks that some code does not throw, for example, in which case the
   * larger (unconstrained) version would be useful.
   */
  private Throwing() {
    /* Not for instanciation. */
  }

  /**
   * Equivalent of {@link java.lang.Runnable} that may throw non-{@code RuntimeException}
   * throwables.
   *
   * @param <X> a sort of throwable that this runnable may throw
   */
  @FunctionalInterface
  public interface Runnable<X extends Throwable> {
    /**
     * Takes an action.
     *
     * @throws X in generally unspecified circumstances
     * @see java.lang.Runnable#run()
     */
    public void run() throws X;
  }

  /**
   * Equivalent of {@link java.util.function.Supplier} that may throw non-{@code RuntimeException}
   * throwables.
   *
   * @param <T> the type of results supplied by this supplier
   * @param <X> a sort of throwable that this supplier may throw
   */
  @FunctionalInterface
  public interface Supplier<T, X extends Throwable> {
    /**
     * Gets a result.
     *
     * @return a result.
     * @throws X in generally unspecified circumstances
     */
    public T get() throws X;
  }

  /**
   * Equivalent of {@link java.util.Comparator} that may throw non-{@code RuntimeException}
   * throwables.
   * <p>
   * TODO add default methods.
   *
   * @param <T> the type of objects that may be compared by this comparator
   * @param <X> a sort of throwable that this comparator may throw
   */
  @FunctionalInterface
  public interface Comparator<T, X extends Throwable> {
    /**
     * Accepts a function that extracts a sort key from a type {@code T}, and returns a
     * {@code Throwing.Comparator<T>} that compares by that sort key using the specified
     * {@link Comparator}.
     *
     * @param <T> the type of element to be compared
     * @param <U> the type of the sort key
     * @param <X> a sort of throwable that the returned comparator may throw
     * @param keyExtractor the function used to extract the sort key
     * @param keyComparator the {@code Throwing.Comparator} used to compare the sort key
     * @return a comparator that compares by an extracted key using the specified
     *         {@code Throwing.Comparator}
     * @see java.util.Comparator#comparing(java.util.Function, java.util.Comparator)
     */
    public static <T, U, X extends Throwable> Throwing.Comparator<T, X> comparing(
        Throwing.Function<? super T, ? extends U, ? extends X> keyExtractor,
        Throwing.Comparator<? super U, ? extends X> keyComparator) {
      checkNotNull(keyExtractor);
      checkNotNull(keyComparator);
      return (c1, c2) -> keyComparator.compare(keyExtractor.apply(c1), keyExtractor.apply(c2));
    }

    /**
     * Accepts a function that extracts a {@link Comparable} sort key from a type {@code T}, and
     * returns a {@code Throwing.Comparator<T>} that compares by that sort key.
     *
     * @param <T> the type of element to be compared
     * @param <U> the type of the {@code Comparable} sort key
     * @param <X> a sort of throwable that the returned comparator may throw
     * @param keyExtractor the function used to extract the {@link Comparable} sort key
     * @return a comparator that compares by an extracted key
     */
    public static <T, U extends Comparable<? super U>, X extends Throwable> Throwing.Comparator<T, X> comparing(
        Throwing.Function<? super T, ? extends U, ? extends X> keyExtractor) {
      checkNotNull(keyExtractor);
      return (c1, c2) -> keyExtractor.apply(c1).compareTo(keyExtractor.apply(c2));
    }

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
     * @throws X in generally unspecified circumstances
     * @see java.util.Comparator#compare(T, T)
     */
    public int compare(T o1, T o2) throws X;

    /**
     * Indicates whether some other object is “equal” to this comparator, implying that the other
     * object is also a {@code Throwing.Comparator} and imposes the same ordering.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} only if the specified object is also a {@code Throwing.Comparator} and
     *         it imposes the same ordering as this comparator.
     * @see java.util.Comparator#equals(Object)
     */
    @Override
    boolean equals(Object obj);

    /**
     * Returns a comparator that imposes the reverse ordering of this comparator.
     *
     * @return a comparator that imposes the reverse ordering of this comparator.
     * @see java.util.Comparator#reversed()
     */
    default Throwing.Comparator<T, X> reversed() {
      return (o1, o2) -> compare(o2, o1);
    }

    /**
     * Returns a lexicographic-order comparator with another comparator.
     *
     * @param other the other comparator to be used when this comparator compares two objects that
     *        are equal.
     * @return a lexicographic-order comparator composed of this and then the other comparator
     * @see java.util.Comparator#thenComparing(java.util.Comparator)
     */
    default Throwing.Comparator<T, X> thenComparing(
        Throwing.Comparator<? super T, ? extends X> other) {
      checkNotNull(other);
      return (c1, c2) -> {
        int res = compare(c1, c2);
        return (res != 0) ? res : other.compare(c1, c2);
      };
    }

    /**
     * Returns a lexicographic-order comparator with a function that extracts a key to be compared
     * with the given {@code Throwing.Comparator}.
     *
     * @param <U> the type of the sort key
     * @param keyExtractor the function used to extract the sort key
     * @param keyComparator the {@code Throwing.Comparator} used to compare the sort key
     * @return a lexicographic-order comparator composed of this comparator and then comparing on
     *         the key extracted by the keyExtractor function
     * @see #thenComparing(Throwing.Comparator)
     * @see java.util.Comparator#thenComparing(java.util.Function, java.util.Comparator)
     */
    default <U> Throwing.Comparator<T, X> thenComparing(
        Throwing.Function<? super T, ? extends U, ? extends X> keyExtractor,
        Throwing.Comparator<? super U, ? extends X> keyComparator) {
      return thenComparing(comparing(keyExtractor, keyComparator));
    }

    /**
     * Returns a lexicographic-order comparator with a function that extracts a {@code Comparable}
     * sort key.
     *
     * @param <U> the type of the {@link Comparable} sort key
     * @param keyExtractor the function used to extract the {@link Comparable} sort key
     * @return a lexicographic-order comparator composed of this and then the {@link Comparable}
     *         sort key.
     * @see #thenComparing(Throwing.Comparator)
     * @see java.util.Comparator#thenComparing(java.util.Function)
     */
    default <U extends Comparable<? super U>> Throwing.Comparator<T, X> thenComparing(
        Throwing.Function<? super T, ? extends U, ? extends X> keyExtractor) {
      return thenComparing(comparing(keyExtractor));
    }
  }

  /**
   * Equivalent of {@link java.util.function.Consumer} that may throw non-{@code RuntimeException}
   * throwables.
   *
   * @param <T> the type of the input to the operation
   * @param <X> a sort of throwable that this consumer may throw
   */
  @FunctionalInterface
  public interface Consumer<T, X extends Throwable> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws X in generally unspecified circumstances
     */
    public void accept(T t) throws X;

    /**
     * Returns a composed {@code Throwing.Consumer} that performs, in sequence, this operation
     * followed by the {@code after} operation.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Throwing.Consumer} that performs in sequence this operation
     *         followed by the {@code after} operation
     * @see java.util.function.Consumer#andThen(java.util.function.Consumer)
     */
    default Throwing.Consumer<T, X> andThen(Throwing.Consumer<? super T, ? extends X> after) {
      checkNotNull(after);
      return t -> {
        accept(t);
        after.accept(t);
      };
    }
  }

  /**
   * Equivalent of {@link java.util.function.BiConsumer} that may throw non-{@code RuntimeException}
   * throwables.
   *
   * @param <T> the type of the first argument to the operation
   * @param <U> the type of the second argument to the operation
   * @param <X> a sort of throwable that the {@code Throwing.BiConsumer} may throw
   */
  @FunctionalInterface
  public interface BiConsumer<T, U, X extends Throwable> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @throws X in generally unspecified circumstances
     */
    public void accept(T t, U u) throws X;
  
    /**
     * Returns a composed {@code Throwing.BiConsumer} that performs, in sequence, this operation
     * followed by the {@code after} operation.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Throwing.BiConsumer} that performs in sequence this operation
     *         followed by the {@code after} operation
     * @see java.util.function.BiConsumer#andThen(java.util.function.BiConsumer)
     */
    default Throwing.BiConsumer<T, U, X> andThen(
        Throwing.BiConsumer<? super T, ? super U, ? extends X> after) {
      checkNotNull(after);
  
      return (l, r) -> {
        accept(l, r);
        after.accept(l, r);
      };
    }
  }

  /**
   * Equivalent of {@link java.util.function.Function} that may throw non-{@code RuntimeException}
   * throwables.
   *
   * @param <T> the type of the input to the function
   * @param <R> the type of the result of the function
   * @param <X> a sort of throwable that the function may throw
   */
  @FunctionalInterface
  public interface Function<T, R, X extends Throwable> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws X in generally unspecified circumstances
     */
    public R apply(T t) throws X;

    /**
     * Returns a composed function that first applies the {@code before} function to its input, and
     * then applies this function to the result.
     *
     * @param <V> the type of input to the {@code before} function, and to the composed function
     * @param before the function to apply before this function is applied
     * @return a composed function that first applies the {@code before} function and then applies
     *         this function
     *
     * @see #andThen(Throwing.Function)
     * @see java.util.function.Function#compose(java.util.function.Function)
     */
    default <V> Throwing.Function<V, R, X> compose(
        Throwing.Function<? super V, ? extends T, ? extends X> before) {
      checkNotNull(before);
      return v -> apply(before.apply(v));
    }

    /**
     * Returns a composed function that first applies this function to its input, and then applies
     * the {@code after} function to the result.
     *
     * @param <V> the type of output of the {@code after} function, and of the composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then applies the
     *         {@code after} function
     *
     * @see #compose(Function)
     * @see java.util.function.Function#andThen(java.util.function.Function)
     */
    default <V> Throwing.Function<T, V, X> andThen(
        Throwing.Function<? super R, ? extends V, ? extends X> after) {
      checkNotNull(after);
      return t -> after.apply(apply(t));
    }
  }


  /**
   * Equivalent of {@link java.util.function.BiFunction} that may throw non-{@code RuntimeException}
   * throwables.
   *
   * @param <T> the type of the first argument to the function
   * @param <U> the type of the second argument to the function
   * @param <R> the type of the result of the function
   * @param <X> a sort of throwable that the {@code Throwing.Function} may throw
   */
  @FunctionalInterface
  public interface BiFunction<T, U, R, X extends Throwable> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     * @throws X in generally unspecified circumstances
     */
    public R apply(T t, U u) throws X;
  
    /**
     * Returns a composed function that first applies this function to its input, and then applies
     * the {@code after} function to the result.
     *
     * @param <V> the type of output of the {@code after} function, and of the composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then applies the
     *         {@code after} function
     * @see java.util.function.BiFunction#andThen(java.util.function.Function)
     */
    default <V> Throwing.BiFunction<T, U, V, X> andThen(
        Throwing.Function<? super R, ? extends V, ? extends X> after) {
      checkNotNull(after);
      return (t, u) -> after.apply(apply(t, u));
    }
  
  }

  /**
   * Equivalent of {@link java.util.function.UnaryOperator} that may throw
   * non-{@code RuntimeException} throwables.
   *
   * @param <T> the type of the operand and result of the operator
   * @param <X> a sort of throwable that the {@code Throwing.UnaryOperator} may throw
   */
  @FunctionalInterface
  public interface UnaryOperator<T, X extends Throwable> extends Throwing.Function<T, T, X> {
  
  }

  /**
   * Equivalent of {@link java.util.function.BinaryOperator} that may throw
   * non-{@code RuntimeException} throwables.
   *
   * @param <T> the type of the operands and result of the operator
   * @param <X> a sort of throwable that the {@code Throwing.BinaryOperator} may throw
   */
  @FunctionalInterface
  public interface BinaryOperator<T, X extends Throwable> extends Throwing.BiFunction<T, T, T, X> {
    /**
     * Returns a {@link Throwing.BinaryOperator} which returns the lesser of two elements according
     * to the specified {@code Comparator}.
     *
     * @param <T> the type of the input arguments of the comparator
     * @param comparator a {@code Throwing.Comparator} for comparing the two values
     * @return a {@code Throwing.BinaryOperator} which returns the lesser of its operands, according
     *         to the supplied {@code Throwing.Comparator}
     */
    public static <T, X extends Throwable> Throwing.BinaryOperator<T, X> minBy(
        Throwing.Comparator<? super T, ? extends X> comparator) {
      checkNotNull(comparator);
      return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }
  
    /**
     * Returns a {@link Throwing.BinaryOperator} which returns the greater of two elements according
     * to the specified {@code Comparator}.
     *
     * @param <T> the type of the input arguments of the comparator
     * @param comparator a {@code Throwing.Comparator} for comparing the two values
     * @return a {@code Throwing.BinaryOperator} which returns the greater of its operands,
     *         according to the supplied {@code Throwing.Comparator}
     */
    public static <T, X extends Throwable> Throwing.BinaryOperator<T, X> maxBy(
        Throwing.Comparator<? super T, ? extends X> comparator) {
      checkNotNull(comparator);
      return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }
  
  }

  /**
   * Equivalent of {@link java.util.function.Predicate} that may throw non-{@code RuntimeException}
   * throwables.
   *
   * @param <T> the type of the input to the predicate
   * @param <X> a sort of throwable that the predicate may throw
   */
  @FunctionalInterface
  public interface Predicate<T, X extends Throwable> {
    /**
     * Returns a throwing predicate that is the negation of the supplied predicate.
     *
     * @param <T> the type of arguments to the specified predicate
     * @param target predicate to negate
     *
     * @return a predicate that negates the results of the supplied predicate
     *
     */
    @SuppressWarnings("unchecked")
    static <T, X extends Throwable> Throwing.Predicate<T, X> not(
        Throwing.Predicate<? super T, ? extends X> target) {
      checkNotNull(target);
      return (Predicate<T, X>) target.negate();
    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     * @throws X in generally unspecified circumstances
     */
    public boolean test(T t) throws X;

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of this predicate
     * and another.
     *
     * @param p2 a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate that represents the short-circuiting logical AND of this
     *         predicate and the {@code other} predicate
     * @see java.util.function.Predicate#and(java.util.function.Predicate)
     */
    public default Throwing.Predicate<T, X> and(Throwing.Predicate<? super T, ? extends X> p2) {
      return t -> test(t) && p2.test(t);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate
     * and another.
     *
     * @param p2 a predicate that will be logically-ORed with this predicate
     * @return a composed predicate that represents the short-circuiting logical OR of this
     *         predicate and the {@code other} predicate
     * @see java.util.function.Predicate#or(java.util.function.Predicate)
     */
    public default Throwing.Predicate<T, X> or(Throwing.Predicate<? super T, ? extends X> p2) {
      return t -> test(t) || p2.test(t);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return a predicate that represents the logical negation of this predicate
     */
    public default Throwing.Predicate<T, X> negate() {
      return t -> !test(t);
    }
  }

  /**
   * Equivalent of {@link java.util.function.BiPredicate} that may throw
   * non-{@code RuntimeException} throwables.
   *
   * @param <T> the type of the first argument to the predicate
   * @param <U> the type of the second argument the predicate
   * @param <X> a sort of throwable that the {@code Throwing.BiPredicate} may throw
   */
  @FunctionalInterface
  public interface BiPredicate<T, U, X extends Throwable> {
    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @return {@code true} if the input arguments match the predicate, otherwise {@code false}
     * @throws X in generally unspecified circumstances
     */
    public boolean test(T t, U u) throws X;

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of this predicate
     * and another.
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate that represents the short-circuiting logical AND of this
     *         predicate and the {@code other} predicate
     * @see java.util.function.BiPredicate#and(java.util.function.BiPredicate)
     */
    default Throwing.BiPredicate<T, U, X> and(
        Throwing.BiPredicate<? super T, ? super U, ? extends X> other) {
      checkNotNull(other);
      return (T t, U u) -> test(t, u) && other.test(t, u);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate
     * and another.
     *
     * @param other a predicate that will be logically-ORed with this predicate
     * @return a composed predicate that represents the short-circuiting logical OR of this
     *         predicate and the {@code other} predicate
     * @see java.util.function.BiPredicate#or(java.util.function.BiPredicate)
     */
    default Throwing.BiPredicate<T, U, X> or(
        Throwing.BiPredicate<? super T, ? super U, ? extends X> other) {
      checkNotNull(other);
      return (T t, U u) -> test(t, u) || other.test(t, u);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return a predicate that represents the logical negation of this predicate
     */
    default Throwing.BiPredicate<T, U, X> negate() {
      return (T t, U u) -> !test(t, u);
    }
  }
}
