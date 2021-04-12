package io.github.oliviercailloux.jaris.exceptions.catch_all.impl;

import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;
import io.github.oliviercailloux.jaris.exceptions.catch_all.TryCatchAll;
import io.github.oliviercailloux.jaris.exceptions.catch_all.TryCatchAllVoid;
import io.github.oliviercailloux.jaris.exceptions.impl.TryOptional;

public class TryVoidCatchAllSuccess extends
    TryOptional.TryVoidVariableCatchSuccess<Throwable, Throwable> implements TryCatchAllVoid {
  public static TryCatchAllVoid given() {
    return new TryVoidCatchAllSuccess();
  }

  private TryVoidCatchAllSuccess() {
    /* Reducing visibility. */
  }

  @Override
  protected boolean catchesAll() {
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
