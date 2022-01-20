package io.github.oliviercailloux.jaris.throwing;

import io.github.oliviercailloux.jaris.exceptions.Throwing;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An enhanced version of {@link Optional}, with methods that accept throwing functionals.
 * <p>
 * Much of the code and documentation here has been adapted from the OpenJDK specification and
 * implementation of {@link Optional}.
 * </p>
 * TODO adapt doc and check signatures.
 *
 * @param <T> the type of value
 * @see Optional
 */
public class TOptional<T> {
  /**
   * Common instance for {@code empty()}.
   */
  private static final TOptional<?> EMPTY = new TOptional<>(null);

  public static <T> TOptional<T> wrapping(Optional<? extends T> delegate) {
    return delegate.<TOptional<T>>map(TOptional::of).orElse(empty());
  }

  /**
   * Returns an empty {@code TOptional} instance.
   *
   * @param <T> The type of the non-existent value
   * @return an empty {@code TOptional}
   */
  public static <T> TOptional<T> empty() {
    @SuppressWarnings("unchecked")
    final TOptional<T> t = (TOptional<T>) EMPTY;
    return t;
  }

  /**
   * Returns a {@code TOptional} describing the given non-{@code null} value.
   *
   * @param value the value to describe, which must be non-{@code null}
   * @param <T> the type of the value
   * @return a {@code TOptional} with the value present
   * @throws NullPointerException if value is {@code null}
   */
  public static <T> TOptional<T> of(T value) {
    return new TOptional<>(Objects.requireNonNull(value));
  }

  /**
   * Returns a {@code TOptional} describing the given value, if non-{@code null}, otherwise returns
   * an empty {@code TOptional}.
   *
   * @param value the possibly-{@code null} value to describe
   * @param <T> the type of the value
   * @return a {@code TOptional} with a present value if the specified value is non-{@code null},
   *         otherwise an empty {@code TOptional}
   */
  public static <T> TOptional<T> ofNullable(T value) {
    return value == null ? empty() : new TOptional<>(value);
  }

  private T value;

  /**
   * Constructs an instance with the described value.
   *
   * @param value {@code null} iff empty.
   */
  private TOptional(T value) {
    this.value = value;
  }

  /**
   * If a value is present, returns {@code true}, otherwise {@code false}.
   *
   * @return {@code true} if a value is present, otherwise {@code false}
   */
  public boolean isPresent() {
    return value != null;
  }

  /**
   * If a value is not present, returns {@code true}, otherwise {@code false}.
   *
   * @return {@code true} if a value is not present, otherwise {@code false}
   */
  public boolean isEmpty() {
    return value == null;
  }

  /**
   * If a value is present, performs the given action with the value, otherwise does nothing.
   *
   * @param action the action to be performed, if a value is present
   * @throws NullPointerException if value is present and the given action is {@code null}
   */
  public <X extends Throwable> void ifPresent(Throwing.Consumer<? super T, X> action) throws X {
    if (value != null) {
      action.accept(value);
    }
  }

  /**
   * If a value is present, performs the given action with the value, otherwise performs the given
   * empty-based action.
   *
   * @param action the action to be performed, if a value is present
   * @param emptyAction the empty-based action to be performed, if no value is present
   * @throws NullPointerException if a value is present and the given action is {@code null}, or no
   *         value is present and the given empty-based action is {@code null}.
   */
  public <X extends Throwable> void ifPresentOrElse(
      Throwing.Consumer<? super T, ? extends X> action, Throwing.Runnable<? extends X> emptyAction)
      throws X {
    if (value != null) {
      action.accept(value);
    } else {
      emptyAction.run();
    }
  }

  /**
   * If a value is present, and the value matches the given predicate, returns an {@code Optional}
   * describing the value, otherwise returns an empty {@code Optional}.
   *
   * @param predicate the predicate to apply to a value, if present
   * @return an {@code Optional} describing the value of this {@code Optional}, if a value is
   *         present and the value matches the given predicate, otherwise an empty {@code Optional}
   * @throws NullPointerException if the predicate is {@code null}
   */
  public <X extends Throwable> TOptional<T> filter(Throwing.Predicate<? super T, X> predicate)
      throws X {
    Objects.requireNonNull(predicate);
    if (!isPresent()) {
      return this;
    }
    return predicate.test(value) ? this : empty();
  }

  /**
   * If a value is present, returns an {@code Optional} describing (as if by {@link #ofNullable})
   * the result of applying the given mapping function to the value, otherwise returns an empty
   * {@code Optional}.
   *
   * <p>
   * If the mapping function returns a {@code null} result then this method returns an empty
   * {@code Optional}.
   *
   * @param mapper the mapping function to apply to a value, if present
   * @param <U> The type of the value returned from the mapping function
   * @return an {@code Optional} describing the result of applying a mapping function to the value
   *         of this {@code Optional}, if a value is present, otherwise an empty {@code Optional}
   * @throws NullPointerException if the mapping function is {@code null}
   */
  public <U, X extends Throwable> TOptional<U>
      map(Throwing.Function<? super T, ? extends U, X> mapper) throws X {
    Objects.requireNonNull(mapper);
    if (!isPresent()) {
      return empty();
    }
    return TOptional.ofNullable(mapper.apply(value));
  }

  /**
   * If a value is present, returns the result of applying the given {@code Optional}-bearing
   * mapping function to the value, otherwise returns an empty {@code Optional}.
   *
   * @param <U> The type of value of the {@code Optional} returned by the mapping function
   * @param mapper the mapping function to apply to a value, if present
   * @return the result of applying an {@code Optional}-bearing mapping function to the value of
   *         this {@code Optional}, if a value is present, otherwise an empty {@code Optional}
   * @throws NullPointerException if the mapping function is {@code null} or returns a {@code null}
   *         result
   */
  public <U, X extends Throwable> TOptional<U>
      flatMap(Throwing.Function<? super T, ? extends TOptional<? extends U>, X> mapper) throws X {
    Objects.requireNonNull(mapper);
    if (!isPresent()) {
      return empty();
    }
    @SuppressWarnings("unchecked")
    final TOptional<U> r = (TOptional<U>) mapper.apply(value);
    return Objects.requireNonNull(r);
  }

  /**
   * If a value is present, returns an {@code Optional} describing the value, otherwise returns an
   * {@code Optional} produced by the supplying function.
   *
   * @param supplier the supplying function that produces an {@code Optional} to be returned
   * @return returns an {@code Optional} describing the value of this {@code Optional}, if a value
   *         is present, otherwise an {@code Optional} produced by the supplying function.
   * @throws NullPointerException if the supplying function is {@code null} or produces a
   *         {@code null} result
   */
  public <X extends Throwable> TOptional<T>
      or(Throwing.Supplier<? extends TOptional<? extends T>, X> supplier) throws X {
    Objects.requireNonNull(supplier);
    if (isPresent()) {
      return this;
    }
    @SuppressWarnings("unchecked")
    final TOptional<T> r = (TOptional<T>) supplier.get();
    return Objects.requireNonNull(r);
  }

  /**
   * If a value is present, returns a sequential {@link Stream} containing only that value,
   * otherwise returns an empty {@code Stream}.
   *
   * @return the optional value as a {@code Stream}
   */
  public Stream<T> stream() {
    if (!isPresent()) {
      return Stream.empty();
    }
    return Stream.of(value);
  }

  /**
   * If a value is present, returns the value, otherwise returns {@code other}.
   *
   * @param other the value to be returned, if no value is present. May be {@code null}.
   * @return the value, if present, otherwise {@code other}
   */
  public T orElse(T other) {
    return value != null ? value : other;
  }

  /**
   * If a value is present, returns the value, otherwise returns the result produced by the
   * supplying function.
   *
   * @param supplier the supplying function that produces a value to be returned
   * @return the value, if present, otherwise the result produced by the supplying function
   * @throws NullPointerException if no value is present and the supplying function is {@code null}
   */
  public <X extends Throwable> T orElseGet(Throwing.Supplier<? extends T, X> supplier) throws X {
    return value != null ? value : supplier.get();
  }

  /**
   * If a value is present, returns the value, otherwise throws {@code NoSuchElementException}.
   *
   * @return the non-{@code null} value described by this {@code Optional}
   * @throws NoSuchElementException if no value is present
   */
  public T orElseThrow() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  /**
   * If a value is present, returns the value, otherwise throws an exception produced by the
   * exception supplying function.
   *
   * @param <X> Type of the exception to be thrown
   * @param exceptionSupplier the supplying function that produces an exception to be thrown
   * @return the value, if present
   * @throws X if no value is present
   * @throws NullPointerException if no value is present and the exception supplying function is
   *         {@code null}
   */
  public <X extends Throwable, Y extends Throwable> T
      orElseThrow(Throwing.Supplier<? extends X, Y> exceptionSupplier) throws X, Y {
    if (value != null) {
      return value;
    }
    throw exceptionSupplier.get();
  }

  /**
   * Indicates whether some other object is "equal to" this {@code Optional}. The other object is
   * considered equal if:
   * <ul>
   * <li>it is also an {@code Optional} and;
   * <li>both instances have no value present or;
   * <li>the present values are "equal to" each other via {@code equals()}.
   * </ul>
   *
   * @param obj an object to be tested for equality
   * @return {@code true} if the other object is "equal to" this object otherwise {@code false}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    return obj instanceof TOptional<?> other && Objects.equals(value, other.value);
  }

  /**
   * Returns the hash code of the value, if present, otherwise {@code 0} (zero) if no value is
   * present.
   *
   * @return hash code value of the present value or {@code 0} if no value is present
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  /**
   * Returns a non-empty string representation of this {@code Optional} suitable for debugging. The
   * exact presentation format is unspecified and may vary between implementations and versions.
   *
   * @return the string representation of this instance
   */
  @Override
  public String toString() {
    return value != null ? String.format("Optional[%s]", value) : "Optional.empty";
  }
}
