package io.github.oliviercailloux.jaris.exceptions;

import java.util.Optional;

/**
 * The root of the {@code Try*} implementation hierarchy, defining in the most general way the
 * concepts of success, failure, and equality.
 * <p>
 * Is homeomorphic to an {@code Optional<T>} xor {@code X}: either is a success, and then
 * <em>may</em> contain a result of type {@code T}, or is a failure, and then <em>does</em> contain
 * a cause of type {@code X}.
 *
 * @param <T> the type of result possibly kept in this object.
 * @param <X> the type of cause kept in this object if it is a failure.
 */
public interface TryOptional<T, X extends Throwable> {

  /**
   * Returns {@code true} iff this instance represents a success.
   *
   * @return {@code true} iff {@link #isFailure()} returns {@code false}
   */
  public boolean isSuccess();

  /**
   * Return {@code true} iff this instance contains a cause.
   *
   * @return {@code true} iff {@link #isSuccess()} returns {@code false}
   */
  public boolean isFailure();

  /**
   * Returns the result contained in this instance. This is necessarily empty if this is a failure,
   * and <em>may</em> be non-empty if this is a success.
   *
   * @return the result contained in this instance as an optional
   */
  public Optional<T> getResult();

  /**
   * Returns the cause contained in this instance.
   *
   * @return a present optional iff this instance is a failure.
   * @see #isFailure()
   */
  public Optional<X> getCause();

  /**
   * Returns {@code true} iff the given object is a {@code TryOptional}, this and the given object
   * are both successes and have equal results, or are both failures and have equal causes.
   *
   * @param o2 the object to compare this instance to
   * @return {@code true} iff {@code o2} is a {@code TryOptional} and this instance and {@code o2}
   *         have equal results (considered as optional) and equal causes (considered as optional)
   */
  @Override
  public boolean equals(Object o2);

  /**
   * Returns a string representation of this object, suitable for debug.
   */
  @Override
  public abstract String toString();
}
