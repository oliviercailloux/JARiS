package io.github.oliviercailloux.jaris.exceptions.catch_all.impl;

import io.github.oliviercailloux.jaris.exceptions.Throwing;
import io.github.oliviercailloux.jaris.exceptions.Throwing.BiFunction;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Function;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;
import io.github.oliviercailloux.jaris.exceptions.catch_all.TryCatchAll;
import io.github.oliviercailloux.jaris.exceptions.impl.TryOptional;

public class TryCatchAllFailure extends TryOptional.TryVariableCatchFailure<Throwable, Throwable>
    implements TryCatchAll<Object> {
  public static <T> TryCatchAll<T> given(Throwable cause) {
    return new TryCatchAllFailure(cause).cast();
  }

  private TryCatchAllFailure(Throwable cause) {
    super(cause);
  }

  @Override
  protected boolean catchesAll() {
    return true;
  }

  private <U> TryCatchAll<U> cast() {
    @SuppressWarnings("unchecked")
    final TryCatchAll<U> casted = (TryCatchAll<U>) this;
    return casted;
  }

  @Override
  public <W extends Exception> TryCatchAll<Object> or(
      Throwing.Supplier<? extends Object, ?> supplier,
      Throwing.BiFunction<? super Throwable, ? super Throwable, ? extends Throwable, W> exceptionsMerger)
      throws W {
    final TryCatchAll<Object> t2 = TryCatchAll.get(supplier);
    return t2.map(TryCatchAll::success, y -> TryCatchAll.failure(exceptionsMerger.apply(cause, y)));
  }

  @Override
  public TryCatchAll<Object> andRun(Throwing.Runnable<?> runnable) {
    return this;
  }

  @Override
  public TryCatchAll<Object> andConsume(Throwing.Consumer<? super Object, ?> consumer) {
    return this;
  }

  @Override
  public <U, V, Y extends Exception> TryCatchAll<V> and(TryCatchAll<U> t2,
      BiFunction<? super Object, ? super U, ? extends V, Y> merger) throws Y {
    return cast();
  }

  @Override
  public <U> TryCatchAll<U> andApply(Throwing.Function<? super Object, ? extends U, ?> mapper) {
    return cast();
  }

}
