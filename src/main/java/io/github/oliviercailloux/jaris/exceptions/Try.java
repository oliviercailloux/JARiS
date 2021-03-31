package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Function;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;


public abstract class Try<T, X extends Exception> extends TryGeneral<T, X> {

  /**
   * Returns a success containing the given result and that will catch only checked exceptions when
   * provided a functional (this matters for the and-based methods).
   *
   * @param <T> the type of result declared to be possibly (and effectively) kept in the returned
   *        instance.
   * @param <X> the type of cause declared to be possibly (but effectively not) kept in the returned
   *        instance.
   * @param t the result to contain
   */
  public static <T, X extends Exception> Try<T, X> success(T t) {
    return new Success<>(t);
  }

  /**
   * Returns a failure containing the given cause and that will catch only checked exceptions when
   * provided a functional (this matters for the or-based methods).
   *
   * @param <T> the type of result declared to be possibly (but effectively not) kept in the
   *        returned instance.
   * @param <X> the type of cause declared to be possibly (and effectively) kept in the returned
   *        instance.
   * @param cause the cause to contain
   */
  public static <T, X extends Exception> Try<T, X> failure(X cause) {
    return new Failure<>(cause);
  }

  /**
   * Attempts to wrap a result from the given supplier.
   * <p>
   * This method returns a failure iff the given supplier throws a checked exception and rethrows
   * the throwable thrown by the supplier if it throws anything else than a checked exception.
   *
   * @param <T> the type of result possibly kept in the returned instance.
   * @param <U> the type of result supplied by this supplier
   * @param <X> the type of cause possibly kept in the returned instance.
   * @param <Y> a sort of exception that the supplier may throw
   * @param supplier the supplier to get a result from
   * @return a success containing the result if the supplier returns a result; a failure containing
   *         the throwable if the supplier throws a checked exception
   */
  public static <T, U extends T, X extends Exception, Y extends X> Try<T, X> get(
      Throwing.Supplier<U, Y> supplier) {
    try {
      return success(supplier.get());
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      @SuppressWarnings("unchecked")
      final Y exc = (Y) e;
      return failure(exc);
    }
  }

  static <T, X extends Exception, U extends T, Y extends X> Try<T, X> cast(Try<U, Y> t) {
    @SuppressWarnings("unchecked")
    final Try<T, X> casted = (Try<T, X>) t;
    return casted;
  }

  private static class Success<T, X extends Exception> extends Try<T, X> {

    private final T result;

    private Success(T result) {
      this.result = checkNotNull(result);
    }

    @Override
    public boolean isSuccess() {
      return true;
    }

    @Override
    public boolean isFailure() {
      return false;
    }

    private <Y extends Exception> Try<T, Y> cast() {
      @SuppressWarnings("unchecked")
      final Try<T, Y> casted = (Try<T, Y>) this;
      return casted;
    }

    @Override
    public <U, Y extends Exception> U map(
        Throwing.Function<T, ? extends U, ? extends Y> transformation,
        Throwing.Function<X, ? extends U, ? extends Y> causeTransformation) throws Y {
      return transformation.apply(result);
    }

    @Override
    public <Y extends Exception> T orMapCause(
        Function<? super X, ? extends T, Y> causeTransformation) throws Y {
      return result;
    }

    @Override
    public <U extends T, Y extends Exception, Z extends Exception> Try<T, Z> or(Try<U, Y> t2,
        BiFunction<X, Y, Z> exceptionsMerger) {
      return cast();
    }

    @Override
    public <Y extends Exception> Optional<T> orConsumeCause(Consumer<? super X, Y> consumer)
        throws Y {
      return Optional.of(result);
    }

    @Override
    public T orThrow() throws X {
      return result;
    }

    @Override
    public <U extends T, Y extends Exception, Z extends Exception> Try<T, Z> orGet(
        Throwing.Supplier<U, Y> supplier, BiFunction<X, Y, Z> exceptionsMerger) {
      return cast();
    }

    @Override
    public <Y extends X> Try<T, X> and(TryVoid<Y> t2) {
      return t2.map(() -> this, Try::failure);
    }

    @Override
    public Try<T, X> andRun(Runnable<? extends X> runnable) {
      return and(TryVoid.run(runnable));
    }

    @Override
    public Try<T, X> andConsume(Consumer<? super T, ? extends X> consumer) {
      return and(TryVoid.run(() -> consumer.accept(result)));
    }

    @Override
    public <U, V, Y extends X> Try<V, X> and(Try<U, Y> t2, BiFunction<T, U, V> merger) {
      return t2.map(u -> success(merger.apply(result, u)), Try::failure);
    }

    @Override
    public <U extends T> Try<U, X> flatMap(Function<T, U, ? extends X> mapper) {
      final Try<U, ? extends X> t2 = Try.get(() -> mapper.apply(result));
      return cast(t2);
    }
  }

  private static class Failure<T, X extends Exception> extends Try<T, X> {
    private final X cause;

    private Failure(X cause) {
      this.cause = checkNotNull(cause);
    }

    @Override
    public boolean isSuccess() {
      return false;
    }

    @Override
    public boolean isFailure() {
      return true;
    }

    private <U> Try<U, X> cast() {
      @SuppressWarnings("unchecked")
      final Try<U, X> casted = (Try<U, X>) this;
      return casted;
    }

    @Override
    public <U, Y extends Exception> U map(
        Throwing.Function<T, ? extends U, ? extends Y> transformation,
        Throwing.Function<X, ? extends U, ? extends Y> causeTransformation) throws Y {
      return causeTransformation.apply(cause);
    }

    @Override
    public <Y extends Exception> T orMapCause(
        Function<? super X, ? extends T, Y> causeTransformation) throws Y {
      return causeTransformation.apply(cause);
    }

    @Override
    public <Y extends Exception> Optional<T> orConsumeCause(Consumer<? super X, Y> consumer)
        throws Y {
      consumer.accept(cause);
      return Optional.empty();
    }

    @Override
    public T orThrow() throws X {
      throw cause;
    }

    @Override
    public <U extends T, Y extends Exception, Z extends Exception> Try<T, Z> or(Try<U, Y> t2,
        BiFunction<X, Y, Z> exceptionsMerger) {
      return t2.map(Try::success, y -> failure(exceptionsMerger.apply(cause, y)));
    }

    @Override
    public <U extends T, Y extends Exception, Z extends Exception> Try<T, Z> orGet(
        Throwing.Supplier<U, Y> supplier, BiFunction<X, Y, Z> exceptionsMerger) {
      final Try<T, Y> t2 = Try.get(supplier);
      return or(t2, exceptionsMerger);
    }

    @Override
    public <Y extends X> Try<T, X> and(TryVoid<Y> t2) {
      return this;
    }

    @Override
    public Try<T, X> andRun(Runnable<? extends X> runnable) {
      return this;
    }

    @Override
    public Try<T, X> andConsume(Consumer<? super T, ? extends X> consumer) {
      return this;
    }

    @Override
    public <U, V, Y extends X> Try<V, X> and(Try<U, Y> t2, BiFunction<T, U, V> merger) {
      return cast();
    }

    @Override
    public <U extends T> Try<U, X> flatMap(Function<T, U, ? extends X> mapper) {
      return cast();
    }
  }

  protected Try() {
    /* Reducing visibility. */
  }

  public abstract <U, Y extends Exception> U map(
      Throwing.Function<T, ? extends U, ? extends Y> transformation,
      Throwing.Function<X, ? extends U, ? extends Y> causeTransformation) throws Y;

  public abstract <Y extends Exception> T orMapCause(
      Throwing.Function<? super X, ? extends T, Y> causeTransformation) throws Y;

  public abstract <Y extends Exception> Optional<T> orConsumeCause(
      Throwing.Consumer<? super X, Y> consumer) throws Y;

  public abstract T orThrow() throws X;

  public abstract <U extends T, Y extends Exception, Z extends Exception> Try<T, Z> or(Try<U, Y> t2,
      BiFunction<X, Y, Z> exceptionsMerger);

  public abstract <U extends T, Y extends Exception, Z extends Exception> Try<T, Z> orGet(
      Throwing.Supplier<U, Y> supplier, BiFunction<X, Y, Z> exceptionsMerger);

  public abstract <Y extends X> Try<T, X> and(TryVoid<Y> t2);

  public abstract Try<T, X> andRun(Throwing.Runnable<? extends X> runnable);

  public abstract Try<T, X> andConsume(Throwing.Consumer<? super T, ? extends X> consumer);

  public abstract <U, V, Y extends X> Try<V, X> and(Try<U, Y> t2, BiFunction<T, U, V> merger);

  public abstract <U extends T> Try<U, X> flatMap(Throwing.Function<T, U, ? extends X> mapper);

  public abstract TryVoid<X> toTryVoid();

  /**
   * Returns <code>true</code> iff, either:
   * <ul>
   * <li>the given object is a {@link Try} and this object and the given one are both successes and
   * hold equal results;
   * <li>the given object is a {@link Try} or a {@link TryVoid} and this object and the given one
   * are both failures and hold equal causes.
   * </ul>
   */
  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof TryOld)) {
      return false;
    }

    final TryOld<?> t2 = (TryOld<?>) o2;
    return Objects.equals(result, t2.result) && Objects.equals(cause, t2.cause);
  }

  @Override
  public int hashCode() {
    return Objects.hash(result, cause);
  }

  /**
   * Returns a string representation of this object, suitable for debug.
   */
  @Override
  public String toString() {
    final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
    if (isSuccess()) {
      stringHelper.add("result", result);
    } else {
      stringHelper.add("cause", cause);
    }
    return stringHelper.toString();
  }

}
