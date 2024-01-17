package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.throwing.TConsumer;

/**
 * Represents either a result or a failure and provides operations to deal with cases of successes
 * and of failures in a unified way.
 * <p>
 * An instance of this class contains either a (non-{@code null}) result, in which case it is called
 * a “success”; or a cause of type {@code X} (some {@link Exception}), in which case it is called a
 * “failure”.
 * </p>
 * <p>
 * Instances of this type are immutable.
 * </p>
 * <p>
 * This type provides transformation operations that admit functional operators that can throw
 * throwables. Some of these methods will catch all throwables thrown by such functional operators,
 * while others will propagate any exception thrown to the caller (see the method documentation).
 * This is the only difference between this type and the {@link Try} type: the latter catches only
 * checked exceptions instead of all throwables.
 * </p>
 * <p>
 * It is generally a bad idea to catch throwables that are not exceptions. Unless dealing with a
 * very specific use case (such as checking correctness of some code), please consider using
 * {@code Try} instead of {@code TryCatchAll}.
 * </p>
 * <p>
 * When the documentation of a method indicates that it catches checked exceptions thrown by some
 * provided functional interface, it is implicit that if the provided functional interface throws
 * anything that is not a checked exception, then it is not caught, and simply thrown back to the
 * caller.
 * </p>
 *
 * @param <T> the type of result possibly kept in the instance.
 */
public interface TryCatchAll<T>
    extends TryVariableCatchInterface<T, Throwable, Throwable> {
  /**
   * Returns a success containing the given result.
   *
   * @param <T> the type of result declared to be (and effectively) kept in the instance
   * @param result the result to contain
   * @return a success
   */
  public static <T> TryCatchAll<T> success() {
    return null;
  }

  @Override
  public abstract TryCatchAll<T> andConsume(TConsumer<? super T, ?> consumer);

}
