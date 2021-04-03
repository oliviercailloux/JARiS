package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Function;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;
import java.util.Optional;

/**
 * <p>
 * An instance of this class represents either a “success” or a “failure”. In the latter case, it
 * contains a cause.
 * </p>
 * <p>
 * Instances of this class are immutable.
 * </p>
 */
public abstract class TryVoid<X extends Exception> extends TryOptionalUnsafe<Object, X>
    implements TryGeneralVoid<X> {
  private static final Success SUCCESS = new Success();

  /**
   * Returns a success.
   *
   * @param <X> the type of cause declared to be possibly (but effectively not) kept in the returned
   *        instance.
   * @param t the result to contain
   */
  public static <X extends Exception> TryVoid<X> success() {
    return SUCCESS.cast();
  }

  /**
   * Returns a failure containing the given cause.
   *
   * @param <X> the type of cause declared to be kept in the returned instance.
   * @param cause the cause to contain
   */
  public static <X extends Exception> TryVoid<X> failure(X cause) {
    return new Failure<>(cause);
  }

  /**
   * Attempts to run the given runnable, and returns a success if it succeeds or a failure
   * containing the checked exception thrown by the runnable if it threw one; otherwise, rethrows
   * the non-checked throwable that the runnable threw.
   *
   * @return a success iff the given runnable did not throw.
   */
  public static <X extends Exception> TryVoid<X> run(Throwing.Runnable<? extends X> runnable) {
    try {
      runnable.run();
      return success();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      @SuppressWarnings("unchecked")
      final X exc = (X) e;
      return TryVoid.failure(exc);
    }
  }

  static <X extends Exception> TryVoid<X> cast(TryVoid<? extends X> t) {
    @SuppressWarnings("unchecked")
    final TryVoid<X> casted = (TryVoid<X>) t;
    return casted;
  }

  private static class Success extends TryVoid<RuntimeException> {

    private Success() {
      /* Reducing visibility. */
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
    Optional<Object> getResult() {
      return Optional.empty();
    }

    @Override
    Optional<RuntimeException> getCause() {
      return Optional.empty();
    }

    private <Y extends Exception> TryVoid<Y> cast() {
      @SuppressWarnings("unchecked")
      final TryVoid<Y> casted = (TryVoid<Y>) this;
      return casted;
    }

    @Override
    public <T, Y extends Exception> T map(Supplier<? extends T, ? extends Y> supplier,
        Function<? super RuntimeException, ? extends T, ? extends Y> causeTransformation) throws Y {
      return supplier.get();
    }

    @Override
    public <Y extends Exception> void ifFailed(Consumer<? super RuntimeException, Y> consumer)
        throws Y {
      /* Nothing to do. */
    }

    @Override
    public void orThrow() {
      /* Nothing to do. */
    }

    @Override
    public <T> Try<T, RuntimeException> andGet(
        Supplier<? extends T, ? extends RuntimeException> supplier) {
      return Try.get(supplier);
    }

    @Override
    public TryVoid<RuntimeException> andRun(Runnable<? extends RuntimeException> runnable) {
      return run(runnable);
    }

    @Override
    public TryVoid<RuntimeException> or(Runnable<? extends RuntimeException> runnable) {
      return this;
    }
  }

  private static class Failure<X extends Exception> extends TryVoid<X> {
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
    Optional<Object> getResult() {
      return Optional.empty();
    }

    @Override
    Optional<X> getCause() {
      return Optional.of(cause);
    }

    @Override
    public <T, Y extends Exception> T map(Supplier<? extends T, ? extends Y> supplier,
        Function<? super X, ? extends T, ? extends Y> causeTransformation) throws Y {
      return causeTransformation.apply(cause);
    }

    @Override
    public <Y extends Exception> void ifFailed(Consumer<? super X, Y> consumer) throws Y {
      consumer.accept(cause);
    }

    @Override
    public void orThrow() throws X {
      throw cause;
    }

    @Override
    public <T> Try<T, X> andGet(Supplier<? extends T, ? extends X> supplier) {
      return Try.failure(cause);
    }

    @Override
    public TryVoid<X> andRun(Runnable<? extends X> runnable) {
      return this;
    }

    @Override
    public TryVoid<X> or(Runnable<? extends X> runnable) {
      return run(runnable);
    }
  }

  /**
   * If this instance is a success, returns a try representing the result of invoking the given
   * supplier; otherwise, returns this failure.
   * <p>
   * If this instance is a failure, it is returned, without invoking the given supplier. Otherwise,
   * the given supplier is invoked. If it terminates without throwing, a success is returned,
   * containing the result just supplied by the supplier. If the supplier throws a checked
   * exception, a failure is returned, containing the cause it threw.
   *
   * @param <T> the type of result that the returned try will be declared to contain
   * @param <X> the type of cause that the returned try will be declared to contain
   * @param supplier the supplier to attempt to get a result from if this instance is a success.
   * @return a success iff this instance is a success and the given supplier terminated without
   *         throwing.
   * @see Try#get(Supplier)
   */
  public abstract <T> Try<T, X> andGet(Throwing.Supplier<? extends T, ? extends X> supplier);

  /**
   * If this instance is a success, returns a {@code TryVoid} instance representing the result of
   * invoking the given runnable; otherwise, returns this failure.
   * <p>
   * If this instance is a failure, it is returned, without invoking the given runnable. Otherwise,
   * the given runnable is invoked. If it terminates without throwing, a success is returned. If the
   * runnable throws a checked exception, a failure is returned, containing the cause it threw.
   *
   * @param <X> the type of cause that the returned instance will be declared to contain
   * @param runnable the runnable to attempt to run if this instance is a success.
   * @return a success iff this instance is a success and the given runnable terminated without
   *         throwing.
   * @see #run(Runnable)
   */
  public abstract TryVoid<X> andRun(Throwing.Runnable<? extends X> runnable);

  /**
   * If this instance is a failure, returns a {@code TryVoid} instance representing the result of
   * invoking the given runnable; otherwise, returns this success.
   * <p>
   * If this instance is a success, it is returned, without invoking the given runnable. Otherwise,
   * the given runnable is invoked. If it terminates without throwing, a success is returned. If the
   * runnable throws a checked exception, a failure is returned, containing the cause it threw.
   *
   * @param <X> the type of cause that the returned instance will be declared to contain
   * @param runnable the runnable to attempt to run if this instance is a failure.
   * @return a success iff this instance is a success or the given runnable terminated without
   *         throwing.
   * @see #run(Runnable)
   */
  public abstract TryVoid<X> or(Throwing.Runnable<? extends X> runnable);

  @Override
  public String toString() {
    final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
    andRun(() -> stringHelper.addValue("success"));
    ifFailed(e -> stringHelper.add("cause", e));
    return stringHelper.toString();
  }

}
