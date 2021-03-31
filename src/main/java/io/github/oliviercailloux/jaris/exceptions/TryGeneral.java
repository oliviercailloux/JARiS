package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An internal try type.
 * <p>
 * An instance can either catch all throwables (“catch-all”) or catch only checked exceptions
 * (“catch-checked”). If it is a catch-all, <code>X</code> must be <code>Throwable</code>. If it is
 * a catch-checked, <code>X</code> can be whatever, but the functionals that can be used with the
 * instance can throw only instances of checked exceptions conforming to <code>X</code>: if
 * <code>X</code> is <code>Exception</code> (or not even), no limit; if <code>X</code> is a sort of
 * checked exception, limited to that checked exception; if <code>X</code> is a sort of runtime
 * exception, the functionals will not be allowed to throw any checked exception. As a result, for
 * catch-checked, there is no advantage for <code>X</code> to be not an <code>Exception</code>.
 *
 * @param <T> the type of result possibly kept in this object.
 * @param <X> the type of cause possibly kept in this object; if this instance is a catch-all, must
 *        equal <code>Throwable</code>; otherwise, must be at least an <code>Exception</code>.
 */
abstract class TryGeneral<T, X extends Throwable> {
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
  public static <T, X extends Exception> TryGeneral<T, X> successCatchingChecked(T t) {
    return new TryGeneralSuccess<>(t, false);
  }

  public static <T> TryGeneral<T, Throwable> successCatchingAll(T t) {
    return new TryGeneralSuccess<>(t, true);
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
  public static <T, X extends Exception> TryGeneral<T, X> failureCatchingChecked(X cause) {
    return new TryGeneralFailure<>(cause, false);
  }

  /**
   * Returns a failure containing the given cause and that will catch all exceptions when provided a
   * functional (this matters for the or-based methods).
   *
   * @param <T> the type of result declared to be possibly (but effectively not) kept in the
   *        returned instance.
   * @param cause the cause to contain
   */
  public static <T> TryGeneral<T, Throwable> failureCatchingAll(Throwable cause) {
    return new TryGeneralFailure<>(cause, true);
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
  public static <T, U extends T, X extends Exception, Y extends X> TryGeneral<T, X> catchingChecked(
      Throwing.Supplier<U, Y> supplier) {
    try {
      return successCatchingChecked(supplier.get());
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      @SuppressWarnings("unchecked")
      final Y exc = (Y) e;
      return failureCatchingChecked(exc);
    }
  }

  /**
   * Attempts to wrap a result from the given supplier.
   * <p>
   * This method returns a failure iff the given supplier throws, irrespective of whether the
   * supplier throws some <code>Y</code> or anything else.
   *
   * @param <T> the type of result possibly kept in the returned instance.
   * @param <U> the type of result supplied by this supplier
   * @param <Y> a sort of exception that the supplier may throw
   * @param supplier the supplier to get a result from
   * @return a success containing the result if the supplier returns a result; a failure containing
   *         the throwable if the supplier throws anything
   */
  public static <T, U extends T, Y extends Exception> TryGeneral<T, Throwable> catchingAll(
      Throwing.Supplier<U, Y> supplier) {
    try {
      return successCatchingAll(supplier.get());
    } catch (Throwable t) {
      return failureCatchingAll(t);
    }
  }

  private static class TryGeneralSuccess<T, X extends Throwable> extends TryGeneral<T, X> {
    private final T result;

    TryGeneralSuccess(T result, boolean catchAll) {
      super(catchAll);
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
  }

  private static class TryGeneralFailure<T, X extends Throwable> extends TryGeneral<T, X> {
    private final X cause;

    TryGeneralFailure(X cause, boolean catchAll) {
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
  }

  /**
   * Guaranteed by the factories that: catchAll ⇒ (X = Throwable), and: ¬catchAll ⇒ (X extends
   * Exception).
   */
  final protected boolean catchAll;

  protected TryGeneral(boolean catchAll) {
    this.catchAll = catchAll;
  }

  /**
   * Returns <code>true</code> iff this object contains a result (and not a cause).
   *
   * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
   */
  public abstract boolean isSuccess();

  /**
   * Return <code>true</code> iff this object contains a cause (and not a result).
   *
   * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
   */
  public abstract boolean isFailure();

  private <Y extends Exception> TryGeneral<T, Y> castSuccessChecked() {
    checkState(isSuccess());
    checkState(!catchAll);
    @SuppressWarnings("unchecked")
    final TryGeneral<T, Y> casted = (TryGeneral<T, Y>) this;
    return casted;
  }

  private TryGeneral<T, Throwable> castSuccessAll() {
    checkState(isSuccess());
    checkState(catchAll);
    @SuppressWarnings("unchecked")
    final TryGeneral<T, Throwable> casted = (TryGeneral<T, Throwable>) this;
    return casted;
  }

  private <Y extends Throwable> TryGeneral<T, X> castSuccessClearer(TryGeneral<T, Y> t2) {
    checkState(t2.isSuccess());
    @SuppressWarnings("unchecked")
    final TryGeneral<T, X> casted = (TryGeneral<T, X>) t2;
    return casted;
  }

  private <Y extends Throwable> TryGeneral<T, Y> castSuccess() {
    checkState(isSuccess());
    @SuppressWarnings("unchecked")
    final TryGeneral<T, Y> casted = (TryGeneral<T, Y>) this;
    return casted;
  }

  private <U> TryGeneral<U, X> castFailure() {
    checkState(isFailure());
    @SuppressWarnings("unchecked")
    final TryGeneral<U, X> casted = (TryGeneral<U, X>) this;
    return casted;
  }

  public <U extends T, Y extends Exception> TryGeneral<T, X> orGet(
      Throwing.Supplier<U, Y> supplier) {
    if (isSuccess()) {
      return this;
    }
    if (catchAll) {
      final TryGeneral<T, Throwable> t2 = TryGeneral.catchingAll(supplier);
      if (t2.isSuccess()) {
        return t2.castSuccessAll();
      }
      return this;
    }
    final TryGeneral<T, Y> t2 = TryGeneral.catchingChecked(supplier);
    if (t2.isSuccess()) {
      return t2;
    }
    return this;
  }

}
