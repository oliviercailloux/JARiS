package io.github.oliviercailloux.jaris.exceptions.old;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import io.github.oliviercailloux.jaris.exceptions.Throwing;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @param <T> the type of result possibly kept in this object.
 */
public class TryOld<T, X extends Exception> {
  /**
   * Attempts to get and encapsulate a result from the given supplier.
   * <p>
   * This method returns a failure iff the given supplier throws, irrespective of whether the
   * supplier throws some <code>X</code> or anything else.
   *
   * @param <T> the type that parameterizes the returned instance
   * @param <U> the type of result supplied by this supplier
   * @param <X> a sort of exception that the supplier may throw
   * @param supplier the supplier to get a result from
   * @return a success containing the result if the supplier returns a result; a failure containing
   *         the throwable if the supplier throws anything
   */
  public static <T, U extends T, X extends Exception, Y extends X> TryOld<T, X> of(
      Throwing.Supplier<U, Y> supplier) {
    try {
      return success(supplier.get());
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      @SuppressWarnings("unchecked")
      final Y exc = (Y) e;
      return TryOld.failure(exc);
    }
  }

  /**
   * Returns a success containing the given result.
   *
   * @param <T> the type that parameterizes the returned instance
   * @param t the result to contain
   */
  public static <T, X extends Exception> TryOld<T, X> success(T t) {
    return new TryOld<>(t, null);
  }

  /**
   * Returns a failure containing the given cause.
   *
   * @param <T> the type that parameterizes the returned instance (mostly irrelevant, as it only
   *        determines which result this instance can hold, but it holds none)
   * @param cause the cause to contain
   */
  public static <T, X extends Exception> TryOld<T, X> failure(X cause) {
    return new TryOld<>(null, cause);
  }

  private final T result;
  private final X cause;

  private TryOld(T t, X cause) {
    final boolean thrown = cause != null;
    final boolean resulted = t != null;
    checkArgument(resulted == !thrown);
    this.cause = cause;
    this.result = t;
  }

  /**
   * Returns <code>true</code> iff this object contains a result (and not a cause).
   *
   * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
   */
  public boolean isSuccess() {
    return result != null;
  }

  /**
   * Return <code>true</code> iff this object contains a cause (and not a result).
   *
   * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
   */
  public boolean isFailure() {
    return cause != null;
  }

  public <U extends T, Y extends X> TryOld<T, X> orGet(Throwing.Supplier<U, Y> supplier) {
    if (isSuccess()) {
      return this;
    }
    final TryOld<T, X> t2 = TryOld.of(supplier);
    if (t2.isSuccess()) {
      return t2;
    }
    return this;
  }

  public <X extends Exception> TryOld<T> and(Throwing.Consumer<T, X> consumer) {
    if (isFailure()) {
      return this;
    }
    final TryVoidOld t2 = TryVoidOld.run(() -> consumer.accept(result));
    if (t2.isFailure()) {
      return failure(t2.getCause());
    }
    return this;
  }

  /**
   * If this instance is a success, returns a {@link TryOld} that contains its result transformed by
   * the given transformation or a cause thrown by the given transformation. If this instance is a
   * failure, returns this instance.
   * <p>
   * This method does not throw. If the given function throws while applying it to this instance’s
   * result, the throwable is returned in the resulting try instance.
   *
   * @param <T2> the type of result the transformation produces.
   * @param transformation the function to apply to the result contained in this instance
   * @return a success iff this instance is a success and the transformation function did not throw
   * @see #map(Function)
   */
  public <T2, X extends Exception> TryOld<T2> andApply(Throwing.Function<T, T2, X> transformation) {
    final TryOld<T2> newResult;
    if (isFailure()) {
      newResult = castFailure();
    } else {
      newResult = TryOld.of(() -> transformation.apply(result));
    }
    return newResult;
  }

  public <T2, X extends Exception, Y extends X, Z extends X> T2 map(
      Throwing.Function<T, T2, Y> transformation,
      Throwing.Function<Throwable, T2, Z> causeTransformation) throws X {
    if (isSuccess()) {
      return transformation.apply(result);
    }
    return causeTransformation.apply(cause);
  }

  /**
   * Returns the result contained in this instance if it is a success; otherwise, returns the result
   * of applying the given transformation to the cause contained in this instance, or throws if the
   * function threw.
   *
   * @param <X> a sort of exception that the given function may throw
   * @param causeTransformation the function to apply to the cause
   * @return the result contained in this instance or the transformed cause
   * @throws X if the given function throws while being applied to the cause contained in this
   *         instance
   */
  public <X extends Exception> T orMapCause(Throwing.Function<Throwable, T, X> causeTransformation)
      throws X {
    checkNotNull(causeTransformation);
    if (isSuccess()) {
      return result;
    }
    return causeTransformation.apply(cause);
  }

  public T orElseThrow() {
    if (isFailure()) {
      throw new NoSuchElementException("This try contains no result");
    }
    return result;
  }

  public <U, R, X extends Exception> TryOld<R> merge(TryOld<U> t2,
      Throwing.BiFunction<T, U, R, X> merger) {
    if (isSuccess()) {
      if (t2.isSuccess()) {
        return TryOld.of(() -> merger.apply(result, t2.result));
      }
      return t2.castFailure();
    }
    return castFailure();
  }

  public Optional<T> toOptional() {
    if (isSuccess()) {
      return Optional.of(result);
    }
    return Optional.empty();
  }

  public TryVoidOld toTryVoid() {
    if (isFailure()) {
      return TryVoidOld.failure(cause);
    }
    return TryVoidOld.success();
  }

  /**
   * Returns <code>true</code> iff the given object is a {@link TryOld}; is a success or a failure
   * according to whether this instance is a success or a failure; and holds an equal result or
   * cause.
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
