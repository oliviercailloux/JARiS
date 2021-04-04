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
public abstract class TrySingleImplVoid<X extends TE, TE extends Throwable>
    extends TrySingleImplOptional<Object, X> {
  private static final Success<Throwable, Throwable> SUCCESS_CATCH_ALL = new Success<>(true);

  private static final Success<Exception, Exception> SUCCESS_CATCH_CHECKED = new Success<>(false);

  static <T> Success<Throwable, Throwable> successCatchAll() {
    return new Success<>(true);
  }

  static <T, X extends Exception> Success<X, Exception> successCatchChecked() {
    return SUCCESS_CATCH_CHECKED.cast();
  }

  static <T> Failure<Throwable, Throwable> failureCatchAll(Throwable cause) {
    return new Failure<>(cause, true);
  }

  static <T, X extends Exception> Failure<X, Exception> failureCatchChecked(X cause) {
    return new Failure<>(cause, false);
  }

  private static class Success<X extends TE, TE extends Throwable>
      extends TrySingleImplVoid<X, TE> {

    private Success(boolean catchAll) {
      super(catchAll);
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
    Optional<X> getCause() {
      return Optional.empty();
    }

    <Y extends TE> TrySingleImplVoid<Y, TE> cast() {
      @SuppressWarnings("unchecked")
      final TrySingleImplVoid<Y, TE> casted = (TrySingleImplVoid<Y, TE>) this;
      return casted;
    }

    @Override
    public <T, Y extends TE> T map(Supplier<? extends T, ? extends Y> supplier,
        Function<? super X, ? extends T, ? extends Y> causeTransformation) throws Y {
      return supplier.get();
    }

    @Override
    public <Y extends TE> void ifFailed(Throwing.Consumer<? super X, Y> consumer) throws Y {
      /* Nothing to do. */
    }

    @Override
    public void orThrow() {
      /* Nothing to do. */
    }

    @Override
    public <T> TrySingleImpl<T, X, TE> andGet(Supplier<? extends T, ? extends X> supplier) {
      TrySingleImplOptional.Builder<X, TE> builder;
      return builder.get(supplier);
    }

    @Override
    public TrySingleImplVoid<X, TE> andRun(Runnable<? extends X> runnable) {
      TrySingleImplOptional.Builder<X, TE> builder;
      return builder.run(runnable);
    }

    @Override
    public TrySingleImplVoid<X, TE> or(Runnable<? extends X> runnable) {
      return this;
    }
  }

  private static class Failure<X extends TE, TE extends Throwable>
      extends TrySingleImplVoid<X, TE> {
    private final X cause;

    private Failure(X cause, boolean catchAll) {
      super(catchAll);
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
    public <T, Y extends TE> T map(Supplier<? extends T, ? extends Y> supplier,
        Function<? super X, ? extends T, ? extends Y> causeTransformation) throws Y {
      return causeTransformation.apply(cause);
    }

    @Override
    public <Y extends TE> void ifFailed(Consumer<? super X, Y> consumer) throws Y {
      consumer.accept(cause);
    }

    @Override
    public void orThrow() throws X {
      throw cause;
    }

    @Override
    public <T> TrySingleImpl<T, X, TE> andGet(Supplier<? extends T, ? extends X> supplier) {
      return Try.failure(cause);
    }

    @Override
    public TrySingleImplVoid<X, TE> andRun(Runnable<? extends X> runnable) {
      return this;
    }

    @Override
    public TrySingleImplVoid<X, TE> or(Runnable<? extends X> runnable) {
      return run(runnable);
    }
  }

  protected TrySingleImplVoid(boolean catchAll) {
    super(catchAll);
  }

  /**
   * Returns the supplied result if this instance is a success, using the provided {@code supplier};
   * or the transformed cause contained in this instance if it is a failure, using the provided
   * {@code causeTransformation}.
   * <p>
   * This method necessarily invokes exactly one of the provided functional interfaces.
   *
   * @param <T> the type of (supplied or transformed) result to return
   * @param <Y> a type of exception that the provided functions may throw
   * @param supplier a supplier to get a result from if this instance is a success
   * @param causeTransformation a function to apply to the cause if this instance is a failure
   * @return the supplied result or transformed cause
   * @throws Y iff the functional interface that was invoked threw a checked exception
   */
  public abstract <T, Y extends TE> T map(Throwing.Supplier<? extends T, ? extends Y> supplier,
      Throwing.Function<? super X, ? extends T, ? extends Y> causeTransformation) throws Y;

  /**
   * If this instance is a failure, invokes the given consumer using the cause contained in this
   * instance. If this instance is a success, do nothing.
   *
   * @param <Y> a type of exception that the provided consumer may throw
   * @param consumer the consumer to invoke if this instance is a failure
   * @throws Y iff the consumer was invoked and threw a checked exception
   */
  public abstract <Y extends TE> void ifFailed(Throwing.Consumer<? super X, Y> consumer) throws Y;

  /**
   * If this instance is a failure, throws the cause it contains. Otherwise, do nothing.
   *
   * @throws X iff this instance contains a cause
   */
  public abstract void orThrow() throws X;

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
  public abstract <T> TrySingleImpl<T, X, TE> andGet(
      Throwing.Supplier<? extends T, ? extends X> supplier);

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
  public abstract TrySingleImplVoid<X, TE> andRun(Throwing.Runnable<? extends X> runnable);

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
  public abstract TrySingleImplVoid<X, TE> or(Throwing.Runnable<? extends X> runnable);

  @Override
  public String toString() {
    final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
    andRun(() -> stringHelper.addValue("success"));
    ifFailed(e -> stringHelper.add("cause", e));
    return stringHelper.toString();
  }

}
