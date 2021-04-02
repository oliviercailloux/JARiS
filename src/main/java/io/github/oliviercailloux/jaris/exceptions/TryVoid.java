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
 * An instance of this class represents either a “success” or a “failure”, in which case it contains
 * a cause.
 * </p>
 * <p>
 * Instances of this class are immutable.
 * </p>
 */
public abstract class TryVoid<X extends Exception> extends TryGeneral<Object, X> {
  private static final Success SUCCESS = new Success();

  public static <X extends Exception> TryVoid<X> success() {
    return SUCCESS.cast();
  }

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
    public <T, Y extends Exception> T map(Supplier<T, ? extends Y> supplier,
        Function<? super RuntimeException, T, ? extends Y> causeTransformation) throws Y {
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
    public <T> Try<T, RuntimeException> andGet(Supplier<T, ? extends RuntimeException> supplier) {
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
    public <T, Y extends Exception> T map(Supplier<T, ? extends Y> supplier,
        Function<? super X, T, ? extends Y> causeTransformation) throws Y {
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
    public <T> Try<T, X> andGet(Supplier<T, ? extends X> supplier) {
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

  public abstract <T, Y extends Exception> T map(Throwing.Supplier<T, ? extends Y> supplier,
      Throwing.Function<? super X, T, ? extends Y> causeTransformation) throws Y;

  public abstract <Y extends Exception> void ifFailed(Throwing.Consumer<? super X, Y> consumer)
      throws Y;

  public abstract void orThrow() throws X;

  public abstract <T> Try<T, X> andGet(Throwing.Supplier<T, ? extends X> supplier);

  public abstract TryVoid<X> andRun(Throwing.Runnable<? extends X> runnable);

  public abstract TryVoid<X> or(Throwing.Runnable<? extends X> runnable);

  @Override
  public String toString() {
    final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
    andRun(() -> stringHelper.addValue("success"));
    ifFailed(e -> stringHelper.add("cause", e));
    return stringHelper.toString();
  }

}
