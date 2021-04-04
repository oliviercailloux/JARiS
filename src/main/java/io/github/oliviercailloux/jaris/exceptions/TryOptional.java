package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Verify.verify;

import io.github.oliviercailloux.jaris.exceptions.old.Try;
import io.github.oliviercailloux.jaris.exceptions.old.TryVoid;
import java.util.Objects;
import java.util.Optional;

/**
 * The root of the Try contract hierarchy, defining in the most general way the concepts of success,
 * failure, catching, and equality.
 * <p>
 * This is not a public part of the contract of Try because cathesAll(), for example, should not be
 * exposed: different catching behaviors are publicly viewed as unrelated types.
 * <p>
 * Is homeomorphic to an {@code Optional<T>} xor {@code X} (plus indication of catching all or
 * checked): either is a success, and then <em>may</em> contain a result of type {@code T}, or is a
 * failure, and then <em>does</em> contain a cause of type {@code X}.
 *
 * @param <T> the type of result possibly kept in this object.
 * @param <X> the type of cause kept in this object if it is a failure.
 */
abstract class TryOptional<T, X extends Throwable> {

  protected TryOptional() {
    /* Reducing visibility. */
  }

  /**
   * Returns <code>true</code> iff this instance represents a success.
   *
   * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
   */
  public boolean isSuccess() {
    final boolean hasResult = getResult().isPresent();
    final boolean hasCause = getCause().isPresent();
    verify(hasResult != hasCause);
    return hasResult;
  }

  /**
   * Return <code>true</code> iff this instance contains a cause.
   *
   * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
   */
  public boolean isFailure() {
    final boolean hasResult = getResult().isPresent();
    final boolean hasCause = getCause().isPresent();
    verify(hasResult != hasCause);
    return hasCause;
  }

  abstract boolean catchesAll();

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
    if (!(o2 instanceof TryOptional)) {
      return false;
    }

    final TryOptional<?, ?> t2 = (TryOptional<?, ?>) o2;
    return getResult().equals(t2.getResult()) && getCause().equals(t2.getCause())
        && (catchesAll() == t2.catchesAll());
  }

  @Override
  public int hashCode() {
    return Objects.hash(catchesAll(), getResult(), getCause());
  }

  /**
   * Returns a string representation of this object, suitable for debug.
   */
  @Override
  public abstract String toString();
}
