package io.github.oliviercailloux.jaris.exceptions;

import java.util.Optional;
import java.util.function.Function;

public interface TryVariableCatchInterface<T, X extends Z, Z extends Throwable> {

  public TryVariableCatchInterface<T, ?, Z>
      andConsume(TConsumer<? super T, ? extends X> consumer);

}