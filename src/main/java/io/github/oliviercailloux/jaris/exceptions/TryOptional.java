package io.github.oliviercailloux.jaris.exceptions;

import java.util.Optional;

/**
 * The root of the {@code Try*} implementation hierarchy, defining in the most general way the
 * concepts of success, failure, catching, and equality.
 * <p>
 * Is homeomorphic to an {@code Optional<T>} xor {@code X}: either is a success, and then
 * <em>may</em> contain a result of type {@code T}, or is a failure, and then <em>does</em> contain
 * a cause of type {@code X}.
 * <p>
 * See this class in branch doc-try for some thoughts about possible extensions; and extensive
 * (draft!) documentation about the design choices.
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

  public Optional<T> getResult();

  public Optional<X> getCause();

  /**
   * Returns {@code true} iff either:
   * <ul>
   * <li>the given object is a {@link Try} and this object and the given one are both successes and
   * hold equal results;
   * <li>the given object is a {@link Try} or a {@link TryVoid} and this object and the given one
   * are both failures and hold equal causes.
   * </ul>
   */
  @Override
  public boolean equals(Object o2);

  /**
   * Returns a string representation of this object, suitable for debug.
   */
  @Override
  public abstract String toString();
}
