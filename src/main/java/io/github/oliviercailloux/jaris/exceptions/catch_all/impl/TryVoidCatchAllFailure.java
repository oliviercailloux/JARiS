package io.github.oliviercailloux.jaris.exceptions.catch_all.impl;

import io.github.oliviercailloux.jaris.exceptions.Throwing;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;
import io.github.oliviercailloux.jaris.exceptions.catch_all.TryCatchAll;
import io.github.oliviercailloux.jaris.exceptions.catch_all.TryCatchAllVoid;
import io.github.oliviercailloux.jaris.exceptions.impl.TryOptional;

public class TryVoidCatchAllFailure extends
    TryOptional.TryVoidVariableCatchFailure<Throwable, Throwable> implements TryCatchAllVoid {
  public static TryCatchAllVoid given(Throwable cause) {
    return new TryVoidCatchAllFailure(cause);
  }

  private TryVoidCatchAllFailure(Throwable cause) {
    super(cause);
  }

  @Override
  protected boolean catchesAll() {
    return true;
  }

  @Override
  public <T> TryCatchAll<T> andGet(Throwing.Supplier<? extends T, ?> supplier) {
    return TryCatchAll.failure(cause);
  }

  @Override
  public TryCatchAllVoid andRun(Runnable<?> runnable) {
    return this;
  }

  @Override
  public TryCatchAllVoid or(Runnable<?> runnable) {
    return TryCatchAllVoid.run(runnable);
  }

}
