package io.github.oliviercailloux.jaris.exceptions;

/**
 * A TryOptional safe, for implementing equals.
 */
public abstract class TryOptionalSafe<T> extends TryOptional<T, Throwable> {

  /**
   * Returns <code>true</code> iff, either:
   * <ul>
   * <li>the given object is a {@link TrySafe} and this object and the given one are both successes
   * and hold equal results;
   * <li>the given object is a {@link TrySafe} or a {@link TryVoidSafe} and this object and the
   * given one are both failures and hold equal causes.
   * </ul>
   */
  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof TryOptionalSafe)) {
      return false;
    }

    final TryOptionalSafe<?> t2 = (TryOptionalSafe<?>) o2;
    return getResult().equals(t2.getResult()) && getCause().equals(t2.getCause());
  }

}
