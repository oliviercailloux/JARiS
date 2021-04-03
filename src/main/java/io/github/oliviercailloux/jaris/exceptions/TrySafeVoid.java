package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;
import java.util.Optional;

/**
 * An equivalent to {@link TryVoid} that catches all throwables instead of catching only checked
 * exceptions.
 */
public abstract class TrySafeVoid extends TryOptionalSafe<Object>
    implements TryGeneralVoid<Throwable> {
  private static final Success SUCCESS = new Success();

  /**
   * Returns a success.
   *
   */
  public static TrySafeVoid success() {
    return SUCCESS;
  }

  /**
   * Returns a failure containing the given cause.
   *
   */
  public static TrySafeVoid failure(Throwable cause) {
    return new Failure(cause);
  }

  /**
   * Attempts to run the given runnable, and returns a success if it succeeds, or a failure
   * containing the cause thrown by the runnable if it threw one.
   *
   * @return a success iff the given runnable did not throw.
   */
  public static TrySafeVoid run(Throwing.Runnable<?> runnable) {
    try {
      runnable.run();
      return success();
    } catch (Throwable t) {
      return failure(t);
    }
  }

  private static class Success extends TrySafeVoid {

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
    Optional<Throwable> getCause() {
      return Optional.empty();
    }

    @Override
    public <T, Y extends Exception> T map(Throwing.Supplier<? extends T, ? extends Y> supplier,
        Throwing.Function<? super Throwable, ? extends T, ? extends Y> causeTransformation)
        throws Y {
      return supplier.get();
    }

    @Override
    public <Y extends Exception> void ifFailed(Consumer<? super Throwable, Y> consumer) throws Y {
      /* Nothing to do. */
    }

    @Override
    public void orThrow() {
      /* Nothing to do. */
    }

    @Override
    public <T> TrySafe<T> andGet(Supplier<? extends T, ?> supplier) {
      return TrySafe.get(supplier);
    }

    @Override
    public TrySafeVoid andRun(Runnable<?> runnable) {
      return run(runnable);
    }

    @Override
    public TrySafeVoid or(Runnable<?> runnable) {
      return this;
    }
  }

  private static class Failure extends TrySafeVoid {
    private final Throwable cause;

    private Failure(Throwable cause) {
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
    Optional<Throwable> getCause() {
      return Optional.of(cause);
    }

    @Override
    public <T, Y extends Exception> T map(Throwing.Supplier<? extends T, ? extends Y> supplier,
        Throwing.Function<? super Throwable, ? extends T, ? extends Y> causeTransformation)
        throws Y {
      return causeTransformation.apply(cause);
    }

    @Override
    public <Y extends Exception> void ifFailed(Consumer<? super Throwable, Y> consumer) throws Y {
      consumer.accept(cause);
    }

    @Override
    public void orThrow() throws Throwable {
      throw cause;
    }

    @Override
    public <T> TrySafe<T> andGet(Supplier<? extends T, ?> supplier) {
      return TrySafe.failure(cause);
    }

    @Override
    public TrySafeVoid andRun(Runnable<?> runnable) {
      return this;
    }

    @Override
    public TrySafeVoid or(Runnable<?> runnable) {
      return run(runnable);
    }
  }

  /**
   * If this instance is a success, returns a try representing the result of invoking the given
   * supplier; otherwise, returns this failure.
   * <p>
   * If this instance is a failure, it is returned, without invoking the given supplier. Otherwise,
   * the given supplier is invoked. If it terminates without throwing, a success is returned,
   * containing the result just supplied by the supplier. Otherwise, a failure is returned,
   * containing the cause that the supplier threw.
   *
   * @param <T> the type of result that the returned try will be declared to contain
   * @param supplier the supplier to attempt to get a result from if this instance is a success.
   * @return a success iff this instance is a success and the given supplier terminated without
   *         throwing.
   * @see TrySafe#get(Supplier)
   */
  public abstract <T> TrySafe<T> andGet(Throwing.Supplier<? extends T, ?> supplier);

  /**
   * If this instance is a success, returns a {@code TrySafeVoid} instance representing the result
   * of invoking the given runnable; otherwise, returns this failure.
   * <p>
   * If this instance is a failure, it is returned, without invoking the given runnable. Otherwise,
   * the given runnable is invoked. If it terminates without throwing, a success is returned.
   * Otherwise, a failure is returned, containing the cause that the runnable threw.
   *
   * @param runnable the runnable to attempt to run if this instance is a success.
   * @return a success iff this instance is a success and the given runnable terminated without
   *         throwing.
   * @see #run(Runnable)
   */
  public abstract TrySafeVoid andRun(Throwing.Runnable<?> runnable);

  /**
   * If this instance is a failure, returns a {@code TrySafeVoid} instance representing the result
   * of invoking the given runnable; otherwise, returns this success.
   * <p>
   * If this instance is a success, it is returned, without invoking the given runnable. Otherwise,
   * the given runnable is invoked. If it terminates without throwing, a success is returned.
   * Otherwise, a failure is returned, containing the cause that the runnable threw.
   *
   * @param runnable the runnable to attempt to run if this instance is a failure.
   * @return a success iff this instance is a success or the given runnable terminated without
   *         throwing.
   * @see #run(Runnable)
   */
  public abstract TrySafeVoid or(Throwing.Runnable<?> runnable);

  @Override
  public String toString() {
    final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
    andRun(() -> stringHelper.addValue("success"));
    ifFailed(e -> stringHelper.add("cause", e));
    return stringHelper.toString();
  }

}
