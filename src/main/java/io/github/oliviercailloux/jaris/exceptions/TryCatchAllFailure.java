package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.Throwing.BiFunction;

public class TryCatchAllFailure extends TryVariableCatchFailure<Throwable, Throwable>
    implements TryCatchAll<Object> {
  public static <T> TryCatchAll<T> given(Throwable cause) {
    return new TryCatchAllFailure(cause).cast();
  }

  private TryCatchAllFailure(Throwable cause) {
    super(cause);
  }

  @Override
  boolean catchesAll() {
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
  public <U> TryCatchAll<U> flatMap(Throwing.Function<? super Object, ? extends U, ?> mapper) {
    return cast();
  }

}
