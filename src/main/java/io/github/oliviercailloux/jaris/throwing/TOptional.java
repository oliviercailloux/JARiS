package io.github.oliviercailloux.jaris.throwing;

import java.util.Objects;
import java.util.Optional;

public class TOptional<T> {
  /**
   * Common instance for {@code empty()}.
   */
  private static final TOptional<?> EMPTY = new TOptional<>(null);

  public static <T> TOptional<T> wrapping(Optional<? extends T> delegate) {

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
  @SuppressWarnings("unchecked")
  public static <T> TOptional<T> ofNullable(T value) {
    return value == null ? (TOptional<T>) EMPTY : new TOptional<>(value);
  }
}
