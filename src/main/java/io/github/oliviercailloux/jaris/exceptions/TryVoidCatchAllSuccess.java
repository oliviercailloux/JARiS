package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;

class TryVoidCatchAllSuccess extends TryVoidVariableCatchSuccess<Throwable, Throwable>
    implements TryCatchAllVoid {
  public static TryCatchAllVoid given() {
    return new TryVoidCatchAllSuccess();
  }

  private TryVoidCatchAllSuccess() {
    /* Reducing visibility. */
  }

  @Override
  boolean catchesAll() {
    return true;
  }

  @Override
  public <T> TryCatchAll<T> andGet(Supplier<? extends T, ?> supplier) {
    return TryCatchAll.get(supplier);
  }

  @Override
  public TryCatchAllVoid andRun(Runnable<?> runnable) {
    return TryCatchAllVoid.run(runnable);
  }

  @Override
  public TryCatchAllVoid or(Runnable<?> runnable) {
    return this;
  }
}
