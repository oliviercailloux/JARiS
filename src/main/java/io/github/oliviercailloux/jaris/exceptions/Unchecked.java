package io.github.oliviercailloux.jaris.exceptions;

public interface Unchecked<X extends Exception, Y extends Exception> {
  /**
   * Calls the given runnable; if it throws a checked exception, throws a transformed exception
   * instead, applying the wrapper; if the runnable throws an unchecked exception, the exception is
   * thrown unchanged.
   *
   * @param runnable the runnable to call
   * @throws Y if the runnable throws a checked exception, or an unchecked exception of type Y
   */
  public void call(Throwing.Runnable<X> runnable) throws Y;

  /**
   * Attempts to get and return a result from the given supplier; if the supplier throws a checked
   * exception, throws a transformed exception instead, applying the wrapper; if the supplier throws
   * an unchecked exception, the exception is thrown unchanged.
   *
   * @param <T> the type returned by the supplier
   * @param supplier the supplier to invoke
   * @return the result obtained from the supplier
   * @throws Y if the supplier throws a checked exception, or an unchecked exception of type Y
   */
  public <T> T getUsing(Throwing.Supplier<T, ? extends X> supplier) throws Y;
}
