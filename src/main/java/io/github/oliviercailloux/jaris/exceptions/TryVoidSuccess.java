package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;

class TryVoidSuccess extends TryVoidVariableCatchSuccess<Exception, Exception>
    implements TryVoid<Exception> {
  public static <X extends Exception> TryVoid<X> given() {
    return new TryVoidSuccess().cast();
  }

  private TryVoidSuccess() {
    /* Reducing visibility. */
  }

  @Override
  boolean catchesAll() {
    return false;
  }

  private <Y extends Exception> TryVoid<Y> cast() {
    @SuppressWarnings("unchecked")
    final TryVoid<Y> casted = (TryVoid<Y>) this;
    return casted;
  }

  @Override
  public <T> Try<T, Exception> andGet(Supplier<? extends T, ? extends Exception> supplier) {
    return Try.get(supplier);
  }

  @Override
  public TryVoid<Exception> andRun(Runnable<? extends Exception> runnable) {
    return TryVoid.run(runnable);
  }

  @Override
  public TryVoid<Exception> or(Runnable<? extends Exception> runnable) {
    return this;
  }
}
