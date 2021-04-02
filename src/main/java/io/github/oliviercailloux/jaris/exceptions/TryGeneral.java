package io.github.oliviercailloux.jaris.exceptions;

import java.util.Objects;
import java.util.Optional;

/**
 * An internal try type.
 * <p>
 * Is homeomorphic to an {@code Optional<T>} xor {@code X}: either is a success, and then
 * <em>may</em> contain a result of type {@code T}, or is a failure, and then <em>does</em> contain
 * a cause of type {@code X}.
 *
 * @param <T> the type of result possibly kept in this object.
 * @param <X> the type of cause possibly kept in this object.
 */
abstract class TryGeneral<T, X extends Exception> {
  protected TryGeneral() {
    /* Reducing visibility. */
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
   * Returns <code>true</code> iff, either:
   * <ul>
   * <li>the given object is a {@link Try} and this object and the given one are both successes and
   * hold equal results;
   * <li>the given object is a {@link Try} or a {@link TryVoid} and this object and the given one
   * are both failures and hold equal causes.
   * </ul>
   */
  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof TryGeneral)) {
      return false;
    }

    final TryGeneral<?, ?> t2 = (TryGeneral<?, ?>) o2;
    return getResult().equals(t2.getResult()) && getCause().equals(t2.getCause());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getResult(), getCause());
  }

  /**
   * Returns a string representation of this object, suitable for debug.
   */
  @Override
  public abstract String toString();

}
