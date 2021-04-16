package io.github.oliviercailloux.jaris.exceptions.catch_all.impl;

import io.github.oliviercailloux.jaris.exceptions.Throwing;
import io.github.oliviercailloux.jaris.exceptions.catch_all.TryCatchAll;
import io.github.oliviercailloux.jaris.exceptions.catch_all.TryCatchAllVoid;
import io.github.oliviercailloux.jaris.exceptions.impl.TryOptional;

public class TryCatchAllImpl {
  public static class TryCatchAllSuccess<T> extends
      TryOptional.TryVariableCatchSuccess<T, Throwable, Throwable> implements TryCatchAll<T> {
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
    public <W extends Exception> TryCatchAll<T> or(Throwing.Supplier<? extends T, ?> supplier,
        Throwing.BiFunction<? super Throwable, ? super Throwable, ? extends Throwable, W> exceptionsMerger)
        throws W {
      return this;
    }

    @Override
    public TryCatchAll<T> andRun(Throwing.Runnable<?> runnable) {
      final TryCatchAllVoid ran = TryCatchAllVoid.run(runnable);
      return ran.map(() -> this, TryCatchAll::failure);
    }

    @Override
    public TryCatchAll<T> andConsume(Throwing.Consumer<? super T, ?> consumer) {
      return andRun(() -> consumer.accept(result));
    }

    @Override
    public <U, V, Y extends Exception> TryCatchAll<V> and(TryCatchAll<U> t2,
        Throwing.BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y {
      return t2.map(u -> TryCatchAll.success(merger.apply(result, u)), TryCatchAll::failure);
    }

    @Override
    public <U> TryCatchAll<U> andApply(Throwing.Function<? super T, ? extends U, ?> mapper) {
      return TryCatchAll.get(() -> mapper.apply(result));
    }
  }

  public static class TryCatchAllFailure extends
      TryOptional.TryVariableCatchFailure<Throwable, Throwable> implements TryCatchAll<Object> {
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
      return t2.map(TryCatchAll::success,
          y -> TryCatchAll.failure(exceptionsMerger.apply(cause, y)));
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
        Throwing.BiFunction<? super Object, ? super U, ? extends V, Y> merger) throws Y {
      return cast();
    }

    @Override
    public <U> TryCatchAll<U> andApply(Throwing.Function<? super Object, ? extends U, ?> mapper) {
      return cast();
    }

  }

  public static class TryVoidCatchAllSuccess extends
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
    public <T> TryCatchAll<T> andGet(Throwing.Supplier<? extends T, ?> supplier) {
      return TryCatchAll.get(supplier);
    }

    @Override
    public TryCatchAllVoid andRun(Throwing.Runnable<?> runnable) {
      return TryCatchAllVoid.run(runnable);
    }

    @Override
    public TryCatchAllVoid or(Throwing.Runnable<?> runnable) {
      return this;
    }
  }

  public static class TryVoidCatchAllFailure extends
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
    public TryCatchAllVoid andRun(Throwing.Runnable<?> runnable) {
      return this;
    }

    @Override
    public TryCatchAllVoid or(Throwing.Runnable<?> runnable) {
      return TryCatchAllVoid.run(runnable);
    }

  }

}
