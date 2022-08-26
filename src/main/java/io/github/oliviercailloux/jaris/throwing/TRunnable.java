package io.github.oliviercailloux.jaris.throwing;

/**
 * Generalization of {@link java.lang.Runnable} that may throw instances of type {@code X}, not just
 * {@code RuntimeException} instances.
 *
 * @param <X> a sort of throwable that this runnable may throw
 */
@FunctionalInterface
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface TRunnable<X extends Throwable> {
  /**
   * Takes an action.
   *
   * @throws X in generally unspecified circumstances
   * @see java.lang.Runnable#run()
   */
  public void run() throws X;
}
