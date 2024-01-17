package io.github.oliviercailloux.jaris.exceptions;

public interface TryCatchAll<T>
    extends TryVariableCatchInterface<T, Throwable, Throwable> {
  public static <T> TryCatchAll<T> instance() {
    return null;
  }

  @Override
  public abstract TryCatchAll<T> andConsume(TConsumer< ?> consumer);

}
