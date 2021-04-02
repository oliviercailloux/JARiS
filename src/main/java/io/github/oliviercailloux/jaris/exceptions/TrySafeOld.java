package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * TODO specific that this library is <code>null</code> hostile
 *
 * @param <T> the type of result possibly kept in this object.
 */
public class TrySafeOld<T> extends TryGeneral<T, Throwable> {
  /**
   * Returns a success containing the given result.
   *
   * @param <T> the type that parameterizes the returned instance
   * @param t the result to contain
   */
  public static <T> TrySafe<T> successSafe(T t) {
    return new TrySafe<>(t, null);
  }

  /**
   * Returns a failure containing the given cause.
   *
   * @param <T> the type that parameterizes the returned instance (mostly irrelevant, as it only
   *        determines which result this instance can hold, but it holds none)
   * @param cause the cause to contain
   */
  public static <T> TrySafe<T> failureSafe(Throwable cause) {
    return new TrySafe<>(null, cause);
  }

  private final T result;
  private final Throwable cause;

  private TrySafeOld(T t, Throwable cause) {
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

  private <T2> TrySafe<T2> castFailure() {
    checkState(isFailure());
    @SuppressWarnings("unchecked")
//    final TrySafe<T2> casted = (TrySafe<T2>) this;
    return casted;
  }

  public <U extends T, X extends Exception> TrySafe<T> orGet(Throwing.Supplier<U, X> supplier) {
    if (isSuccess()) {
      return this;
    }
    final TrySafe<T> t2 = TrySafe.of(supplier);
    if (t2.isSuccess()) {
      return t2;
    }
    return this;
  }

  public <X extends Exception> TrySafe<T> and(Throwing.Consumer<T, X> consumer) {
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
   * If this instance is a success, returns a {@link TrySafe} that contains its result transformed
   * by the given transformation or a cause thrown by the given transformation. If this instance is
   * a failure, returns this instance.
   * <p>
   * This method does not throw. If the given function throws while applying it to this instance’s
   * result, the throwable is returned in the resulting try instance.
   *
   * @param <T2> the type of result the transformation produces.
   * @param transformation the function to apply to the result contained in this instance
   * @return a success iff this instance is a success and the transformation function did not throw
   * @see #map(Function)
   */
  public <T2, X extends Exception> TrySafe<T2> flatMap(Throwing.Function<T, T2, X> transformation) {
    final TrySafe<T2> newResult;
    if (isFailure()) {
      newResult = castFailure();
    } else {
      newResult = TrySafe.of(() -> transformation.apply(result));
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

  public <U, R, X extends Exception> TrySafe<R> merge(TrySafe<U> t2,
      Throwing.BiFunction<T, U, R, X> merger) {
    if (isSuccess()) {
      if (t2.isSuccess()) {
        return TrySafe.of(() -> merger.apply(result, t2.result));
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
   * Returns <code>true</code> iff the given object is a {@link TrySafe}; is a success or a failure
   * according to whether this instance is a success or a failure; and holds an equal result or
   * cause.
   */
  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof TrySafe)) {
      return false;
    }

    final TrySafe<?> t2 = (TrySafe<?>) o2;
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
