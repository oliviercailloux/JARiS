package io.github.oliviercailloux.jaris.exceptions;

public interface TryCatchAll
    extends TryVariableCatchInterface<Throwable, Throwable> {
  public static TryCatchAll instance() {
    return null;
  }

  @Override
  public abstract TryCatchAll andConsume(TConsumer<?> consumer);

}
