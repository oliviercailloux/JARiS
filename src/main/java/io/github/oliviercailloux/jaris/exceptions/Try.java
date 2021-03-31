package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Function;
import java.util.function.BiFunction;


abstract class Try<T, X extends Exception> extends TryGeneral<T, X> {

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
    public <T2, Y extends Exception, Z1 extends Y, Z2 extends Y> T2 map(
        Throwing.Function<T, T2, Z1> transformation,
        Throwing.Function<X, T2, Z2> causeTransformation) throws Y {
      return transformation.apply(result);
    }

    @Override
    public <U extends T, Y extends Exception, Z extends Exception> Try<T, Z> or(Try<U, Y> t2,
        BiFunction<X, Y, Z> exceptionsMerger) {
      return cast();
    }

    @Override
    public <U extends T, Y extends Exception, Z extends Exception> Try<T, Z> orGet(
        Throwing.Supplier<U, Y> supplier, BiFunction<X, Y, Z> exceptionsMerger) {
      return cast();
    }

    @Override
    public <U, V, Y extends X> Try<V, X> and(Try<U, Y> t2, BiFunction<T, U, V> merger) {
      return t2.map(u -> success(merger.apply(result, u)), Try::failure);
    }

    @Override
    public Try<T, X> andIfPresent(Consumer<? super T, ? extends X> consumer) {
      // consumer.accept(result);
      // return null;
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
    public <T2, Y extends Exception, Z1 extends Y, Z2 extends Y> T2 map(
        Throwing.Function<T, T2, Z1> transformation,
        Throwing.Function<X, T2, Z2> causeTransformation) throws Y {
      return causeTransformation.apply(cause);
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
    public <U, V, Y extends X> Try<V, X> and(Try<U, Y> t2, BiFunction<T, U, V> merger) {
      return cast();
    }

    @Override
    public Try<T, X> andIfPresent(Consumer<? super T, ? extends X> consumer) {
      return this;
    }

    @Override
    public <U extends T> Try<U, X> flatMap(Function<T, U, ? extends X> mapper) {
      return cast();
    }
  }

  protected Try() {
    /* Reducing visibility. */
  }

  public abstract <T2, Y extends Exception, Z1 extends Y, Z2 extends Y> T2 map(
      Throwing.Function<T, T2, Z1> transformation, Throwing.Function<X, T2, Z2> causeTransformation)
      throws Y;

  public abstract <U extends T, Y extends Exception, Z extends Exception> Try<T, Z> or(Try<U, Y> t2,
      BiFunction<X, Y, Z> exceptionsMerger);

  public abstract <U extends T, Y extends Exception, Z extends Exception> Try<T, Z> orGet(
      Throwing.Supplier<U, Y> supplier, BiFunction<X, Y, Z> exceptionsMerger);

  public abstract <U, V, Y extends X> Try<V, X> and(Try<U, Y> t2, BiFunction<T, U, V> merger);

  public abstract Try<T, X> andIfPresent(Throwing.Consumer<? super T, ? extends X> consumer);

  public abstract <U extends T> Try<U, X> flatMap(Throwing.Function<T, U, ? extends X> mapper);

}
