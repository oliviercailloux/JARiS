package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;

public class TryVoidFailure<X extends Exception> extends TryVoidVariableCatchFailure<X, Exception>
    implements TryVoid<X> {
  public static <X extends Exception> TryVoid<X> given(X cause) {
    return new TryVoidFailure<>(cause);
  }

  private TryVoidFailure(X cause) {
    super(cause);
  }

  @Override
  boolean catchesAll() {
    return false;
  }

  @Override
  public <T> Try<T, X> andGet(Supplier<? extends T, ? extends X> supplier) {
    return Try.failure(cause);
  }

  @Override
  public TryVoid<X> andRun(Runnable<? extends X> runnable) {
    return this;
  }

  @Override
  public TryVoid<X> or(Runnable<? extends X> runnable) {
    return TryVoid.run(runnable);
  }

}
