package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Function;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import java.util.Optional;
import java.util.function.BiFunction;


public abstract class Try<T, X extends Exception> extends TryGeneral<T, X> {

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /**
   * A builder of Try failures, purely for syntactic sugar.
   *
   * @param <T> the type of result declared to be possibly (but effectively not) kept in the built
   *        instances.
   */
  public static class Builder<T> {
    private Builder() {
      /* Reducing visibility. */
    }

    /**
     * Returns a failure containing the given cause and that will catch only checked exceptions when
     * provided a functional (this matters for the or-based methods).
     *
     * @param <X> the type of cause declared to be possibly (and effectively) kept in the returned
     *        instance.
     * @param cause the cause to contain
     */
    public <X extends Exception> Try<T, X> failure(X cause) {
      return new Failure<>(cause);
    }
  }

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

    @Override
    Optional<T> getResult() {
      return Optional.of(result);
    }

    @Override
    Optional<X> getCause() {
      return Optional.empty();
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
    public Try<T, X> andRun(Runnable<? extends X> runnable) {
      final TryVoid<? extends X> ran = TryVoid.run(runnable);
      return ran.map(() -> this, Try::failure);
    }

    @Override
    public Try<T, X> andConsume(Consumer<? super T, ? extends X> consumer) {
      return andRun(() -> consumer.accept(result));
    }

    @Override
    public <U, V, Y extends X, Z extends X> Try<V, X> and(Try<U, Y> t2,
        Throwing.BiFunction<T, U, V, Z> merger) throws Z {
      return t2.map(u -> success(merger.apply(result, u)), Try::failure);
    }

    @Override
    public <U extends T> Try<U, X> flatMap(Function<T, U, ? extends X> mapper) {
      final Try<U, ? extends X> t2 = Try.get(() -> mapper.apply(result));
      return cast(t2);
    }

    @Override
    public TryVoid<X> toTryVoid() {
      return TryVoid.success();
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

    @Override
    Optional<T> getResult() {
      return Optional.empty();
    }

    @Override
    Optional<X> getCause() {
      return Optional.of(cause);
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
    public Try<T, X> andRun(Runnable<? extends X> runnable) {
      return this;
    }

    @Override
    public Try<T, X> andConsume(Consumer<? super T, ? extends X> consumer) {
      return this;
    }

    @Override
    public <U, V, Y extends X, Z extends X> Try<V, X> and(Try<U, Y> t2,
        Throwing.BiFunction<T, U, V, Z> merger) throws Z {
      return cast();
    }

    @Override
    public <U extends T> Try<U, X> flatMap(Function<T, U, ? extends X> mapper) {
      return cast();
    }

    @Override
    public TryVoid<X> toTryVoid() {
      return TryVoid.failure(cause);
    }
  }

  protected Try() {
    /* Reducing visibility. */
  }

  /**
   * Returns <code>true</code> iff this object contains a result (and not a cause).
   */
  @Override
  public abstract boolean isSuccess();

  /**
   * Return <code>true</code> iff this object contains a cause (and not a result).
   */
  @Override
  public abstract boolean isFailure();

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

  public abstract Try<T, X> andRun(Throwing.Runnable<? extends X> runnable);

  public abstract Try<T, X> andConsume(Throwing.Consumer<? super T, ? extends X> consumer);

  public abstract <U, V, Y extends X, Z extends X> Try<V, X> and(Try<U, Y> t2,
      Throwing.BiFunction<T, U, V, Z> merger) throws Z;

  public abstract <U extends T> Try<U, X> flatMap(Throwing.Function<T, U, ? extends X> mapper);

  public abstract TryVoid<X> toTryVoid();

  @Override
  public String toString() {
    final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
    andConsume(r -> stringHelper.add("result", r))
        .orConsumeCause(e -> stringHelper.add("cause", e));
    return stringHelper.toString();
  }

}
