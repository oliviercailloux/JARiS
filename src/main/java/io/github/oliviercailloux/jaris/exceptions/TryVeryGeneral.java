package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An internal try type.
 * <p>
 * Is homeomorphic to an {@code Optional<T>} xor {@code X}: either is a success, and then
 * <em>may</em> contain a result of type {@code T}, or is a failure, and then <em>does</em> contain
 * a cause of type {@code X}.
 *
 * @param <T> the type of result possibly kept in this object.
 * @param <X> the type of cause possibly kept in this object; if this instance is a catch-all, must
 *        equal <code>Throwable</code>; otherwise, must be at least an <code>Exception</code>.
 */
abstract class TryVeryGeneral<T, X extends Throwable> {
  public static <T> TryVeryGeneral<T, Throwable> successCatchingAll(T t) {
    return new TryGeneralSuccess<>(t, true);
  }

  /**
   * Returns a failure containing the given cause and that will catch all exceptions when provided a
   * functional (this matters for the or-based methods).
   *
   * @param <T> the type of result declared to be possibly (but effectively not) kept in the
   *        returned instance.
   * @param cause the cause to contain
   */
  public static <T> TryVeryGeneral<T, Throwable> failureCatchingAll(Throwable cause) {
    return new TryGeneralFailure<>(cause, true);
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
  public static <T, U extends T, Y extends Exception> TryVeryGeneral<T, Throwable> catchingAll(
      Throwing.Supplier<U, Y> supplier) {
    try {
      return successCatchingAll(supplier.get());
    } catch (Throwable t) {
      return failureCatchingAll(t);
    }
  }

  private static class TrySafely<T> extends TryVeryGeneral<T, Throwable> {

    private static class Success<T> extends TrySafely<T> {

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

      public <U extends T, Y extends Exception> TryVeryGeneral<T, X> orGet(
          Throwing.Supplier<U, Y> supplier) {
        return this;
      }
    }

    private static class Failure<T, X extends Throwable> extends TryVeryGeneral<T, X> {
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

      public <U extends T, Y extends Exception> TryVeryGeneral<T, X> orGet(
          Throwing.Supplier<U, Y> supplier) {
        if (catchAll) {
          final TryVeryGeneral<T, Throwable> t2 = TryVeryGeneral.catchingAll(supplier);
          if (t2.isSuccess()) {
            return t2.castSuccessAll();
          }
          return this;
        }
        final TryVeryGeneral<T, Y> t2 = TryVeryGeneral.catchingChecked(supplier);
        if (t2.isSuccess()) {
          return t2;
        }
        return this;
      }
    }

    protected Try() {
      /* Reducing visibility. */
    }

  }

  protected TryVeryGeneral() {
    /* Reducing visibility. */
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

  TryVeryGeneral<T, Throwable> castSuccessAll() {
    checkState(isSuccess());
    checkState(catchAll);
    @SuppressWarnings("unchecked")
    final TryVeryGeneral<T, Throwable> casted = (TryVeryGeneral<T, Throwable>) this;
    return casted;
  }

  private <Y extends Throwable> TryVeryGeneral<T, X> castSuccessClearer(TryVeryGeneral<T, Y> t2) {
    checkState(t2.isSuccess());
    @SuppressWarnings("unchecked")
    final TryVeryGeneral<T, X> casted = (TryVeryGeneral<T, X>) t2;
    return casted;
  }

  private <Y extends Throwable> TryVeryGeneral<T, Y> castSuccess() {
    checkState(isSuccess());
    @SuppressWarnings("unchecked")
    final TryVeryGeneral<T, Y> casted = (TryVeryGeneral<T, Y>) this;
    return casted;
  }

  private <U> TryVeryGeneral<U, X> castFailure() {
    checkState(isFailure());
    @SuppressWarnings("unchecked")
    final TryVeryGeneral<U, X> casted = (TryVeryGeneral<U, X>) this;
    return casted;
  }

}
