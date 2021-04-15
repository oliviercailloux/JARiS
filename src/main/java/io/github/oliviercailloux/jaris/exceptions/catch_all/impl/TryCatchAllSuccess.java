package io.github.oliviercailloux.jaris.exceptions.catch_all.impl;

import io.github.oliviercailloux.jaris.exceptions.Throwing;
import io.github.oliviercailloux.jaris.exceptions.Throwing.BiFunction;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Function;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;
import io.github.oliviercailloux.jaris.exceptions.catch_all.TryCatchAll;
import io.github.oliviercailloux.jaris.exceptions.catch_all.TryCatchAllVoid;
import io.github.oliviercailloux.jaris.exceptions.impl.TryOptional;

public class TryCatchAllSuccess<T>
    extends TryOptional.TryVariableCatchSuccess<T, Throwable, Throwable> implements TryCatchAll<T> {
  public static <T> TryCatchAll<T> given(T result) {
    return new TryCatchAllSuccess<>(result);
  }

  private TryCatchAllSuccess(T result) {
    super(result);
  }

  @Override
  protected boolean catchesAll() {
    return true;
  }

  @Override
  public <W extends Exception> TryCatchAll<T> or(Supplier<? extends T, ?> supplier,
      BiFunction<? super Throwable, ? super Throwable, ? extends Throwable, W> exceptionsMerger)
      throws W {
    return this;
  }

  @Override
  public TryCatchAll<T> andRun(Runnable<?> runnable) {
    final TryCatchAllVoid ran = TryCatchAllVoid.run(runnable);
    return ran.map(() -> this, TryCatchAll::failure);
  }

  @Override
  public TryCatchAll<T> andConsume(Consumer<? super T, ?> consumer) {
    return andRun(() -> consumer.accept(result));
  }

  @Override
  public <U, V, Y extends Exception> TryCatchAll<V> and(TryCatchAll<U> t2,
      BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y {
    return t2.map(u -> TryCatchAll.success(merger.apply(result, u)), TryCatchAll::failure);
  }

  @Override
  public <U> TryCatchAll<U> andApply(Function<? super T, ? extends U, ?> mapper) {
    return TryCatchAll.get(() -> mapper.apply(result));
  }
}
