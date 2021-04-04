package io.github.oliviercailloux.jaris.exceptions;

import java.util.Objects;
import java.util.Optional;

/**
 * An internal try type for extension by Try and TryVoid and the safe versions.
 * <p>
 * Is homeomorphic to an {@code Optional<T>} xor {@code X} (plus indication of catching all or
 * checked): either is a success, and then <em>may</em> contain a result of type {@code T}, or is a
 * failure, and then <em>does</em> contain a cause of type {@code X}.
 *
 * @param <T> the type of result possibly kept in this object.
 * @param <X> the type of cause possibly kept in this object.
 */
abstract class TrySingleImplOptional<T, X extends Throwable> {

  public static interface Builder<TE extends Throwable> {
    public <T, X extends TE> TrySingleImpl<T, X, TE> success(T t);

    public <T, X extends TE> TrySingleImpl<T, X, TE> failure(X cause);

    public <T, X extends TE> TrySingleImpl<T, X, TE> get(
        Throwing.Supplier<? extends T, ? extends X> supplier);

    public <T, X extends TE> TrySingleImplVoid<X, TE> success();

    public <X extends TE> TrySingleImplVoid<X, TE> failureVoid(X cause);

    public <X extends TE> TrySingleImplVoid<X, TE> run(Throwing.Runnable<? extends X> runnable);
  }

  public static class BuilderCatchingAll implements Builder<Throwable> {
    @Override
    public <T, X extends Throwable> TrySingleImpl<T, X, Throwable> success(T t) {
      return TrySingleImpl.successCatchAll(t);
    }

    @Override
    public <T, X extends Throwable> TrySingleImpl<T, Throwable, Throwable> failure(
        Throwable cause) {
      return TrySingleImpl.failureCatchAll(cause);
    }

    @Override
    public <T> TrySingleImpl<T, Throwable, Throwable> get(
        Throwing.Supplier<? extends T, ?> supplier) {
      try {
        return success(supplier.get());
      } catch (Throwable e) {
        return failure(e);
      }
    }

    @Override
    public TrySingleImplVoid<Throwable, Throwable> success() {
      return TrySingleImplVoid.successCatchAll();
    }

    @Override
    public TrySingleImplVoid<Throwable, Throwable> failureVoid(Throwable cause) {
      return TrySingleImplVoid.failureCatchAll(cause);
    }

    @Override
    public TrySingleImplVoid<Throwable, Throwable> run(Throwing.Runnable<?> runnable) {
      try {
        runnable.run();
      } catch (Throwable e) {
        return failureVoid(e);
      }
      return success();
    }

  }

  public static class BuilderCatchingChecked<X extends Exception> implements Builder<X, Exception> {
    @Override
    public <T> TrySingleImpl<T, X, Exception> success(T t) {
      return TrySingleImpl.successCatchChecked(t);
    }

    @Override
    public <T> TrySingleImpl<T, X, Exception> failure(X cause) {
      return TrySingleImpl.failureCatchChecked(cause);
    }

    @Override
    public <T> TrySingleImpl<T, X, Exception> get(
        Throwing.Supplier<? extends T, ? extends X> supplier) {
      try {
        return success(supplier.get());
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        @SuppressWarnings("unchecked")
        final X exc = (X) e;
        return failure(exc);
      }
    }

    @Override
    public TrySingleImplVoid<X, Exception> success() {
      return TrySingleImplVoid.successCatchChecked();
    }

    @Override
    public TrySingleImplVoid<X, Exception> failureVoid(X cause) {
      return TrySingleImplVoid.failureCatchChecked(cause);
    }

    @Override
    public TrySingleImplVoid<X, Exception> run(Throwing.Runnable<? extends X> runnable) {
      try {
        runnable.run();
        return success();
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        @SuppressWarnings("unchecked")
        final X exc = (X) e;
        return TrySingleImplVoid.failureCatchChecked(exc);
      }
    }

  }

  private final boolean catchAll;

  protected TrySingleImplOptional(boolean catchAll) {
    this.catchAll = catchAll;
  }

  /**
   * Returns <code>true</code> iff this instance represents a success.
   *
   * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
   */
  public abstract boolean isSuccess();

  /**
   * Return <code>true</code> iff this instance contains a cause.
   *
   * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
   */
  public abstract boolean isFailure();

  abstract Optional<T> getResult();

  abstract Optional<X> getCause();

  /**
   * Returns <code>true</code> iff both instances have the same “catching” behavior, and either:
   * <ul>
   * <li>the given object is a {@link Try} and this object and the given one are both successes and
   * hold equal results;
   * <li>the given object is a {@link Try} or a {@link TryVoid} and this object and the given one
   * are both failures and hold equal causes.
   * </ul>
   */
  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof TrySingleImplOptional)) {
      return false;
    }

    final TrySingleImplOptional<?, ?> t2 = (TrySingleImplOptional<?, ?>) o2;
    return (catchAll == t2.catchAll) && getResult().equals(t2.getResult())
        && getCause().equals(t2.getCause());
  }

  @Override
  public int hashCode() {
    return Objects.hash(catchAll, getResult(), getCause());
  }

  /**
   * Returns a string representation of this object, suitable for debug.
   */
  @Override
  public abstract String toString();
}
