package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * An instance of this class represents either a “success” or a “failure”, in which case it contains
 * a cause of type {@link Throwable}.
 * </p>
 * <p>
 * Instances of this class are immutable.
 * </p>
 */
public class TryVoid {
  private static final TryVoid SUCCESS = new TryVoid(null);

  /**
   * Attempts to run the given runnable, and returns a success if it succeeds; or a failure
   * containing the exception thrown by the runnable if it threw one.
   *
   * @return a success iff the given runnable did not throw an exception.
   */
  public static <X extends Exception> TryVoid run(Throwing.Runnable<X> runnable) {
    try {
      runnable.run();
      return success();
    } catch (Throwable t) {
      return TryVoid.failure(t);
    }
  }

  /**
   * Returns the instance that represents a success.
   */
  public static TryVoid success() {
    return SUCCESS;
  }

  /**
   * Returns a failure containing the given cause.
   */
  public static TryVoid failure(Throwable cause) {
    return new TryVoid(checkNotNull(cause));
  }

  private final Throwable cause;

  private TryVoid(Throwable t) {
    this.cause = t;
  }

  /**
   * Returns <code>true</code> iff this object represents a success.
   *
   * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
   */
  public boolean isSuccess() {
    return cause == null;
  }

  /**
   * Returns <code>true</code> iff this object represents a failure; equivalently, iff it
   * encapsulates a cause.
   *
   * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
   */
  public boolean isFailure() {
    return cause != null;
  }

  /**
   * If this object is a failure, returns the cause it encapsulates.
   *
   * @throws IllegalStateException if this object is a success, thus, encapsulates no cause.
   * @see #isFailure()
   */
  public Throwable getCause() throws IllegalStateException {
    checkState(isFailure());
    return cause;
  }

  public <T, X extends Exception> Try<T> andGet(Throwing.Supplier<T, X> supplier) {
    if (isSuccess()) {
      return Try.of(supplier);
    }
    return Try.failure(cause);
  }

  public Optional<Throwable> asOptional() {
    return Optional.ofNullable(cause);
  }

  /**
   * Returns <code>true</code> iff the given object is a {@link Try} and, either: this object and
   * the given object are both successes; or this object and the given object are both failures and
   * they encapsulate equal causes.
   */
  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof TryVoid)) {
      return false;
    }

    final TryVoid t2 = (TryVoid) o2;
    return Objects.equals(cause, t2.cause);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cause);
  }

  /**
   * Returns a string representation of this object, suitable for debug.
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("t", cause).toString();
  }

}
