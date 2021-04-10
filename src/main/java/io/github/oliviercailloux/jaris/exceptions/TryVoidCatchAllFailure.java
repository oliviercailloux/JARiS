package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;

public class TryVoidCatchAllFailure extends TryVoidVariableCatchFailure<Throwable, Throwable>
    implements TryCatchAllVoid {
  public static TryCatchAllVoid given(Throwable cause) {
    return new TryVoidCatchAllFailure(cause);
  }

  private TryVoidCatchAllFailure(Throwable cause) {
    super(cause);
  }

  @Override
  boolean catchesAll() {
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
