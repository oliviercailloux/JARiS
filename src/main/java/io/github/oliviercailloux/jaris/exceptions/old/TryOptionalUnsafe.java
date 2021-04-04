package io.github.oliviercailloux.jaris.exceptions.old;

/**
 * A TryOptional unsafe, for implementing equals.
 */
public abstract class TryOptionalUnsafe<T, X extends Exception> extends TryOptional<T, X> {

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
    if (!(o2 instanceof TryOptionalUnsafe)) {
      return false;
    }

    final TryOptionalUnsafe<?, ?> t2 = (TryOptionalUnsafe<?, ?>) o2;
    return getResult().equals(t2.getResult()) && getCause().equals(t2.getCause());
  }

}
