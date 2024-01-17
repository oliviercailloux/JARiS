package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.throwing.TConsumer;

public interface TryCatchAll<T>
    extends TryVariableCatchInterface<T, Throwable, Throwable> {
  public static <T> TryCatchAll<T> instance() {
    return null;
  }

  @Override
  public abstract TryCatchAll<T> andConsume(TConsumer<? super T, ?> consumer);

}
