package io.github.oliviercailloux.jaris.exceptions;

import java.util.Optional;
import java.util.function.Function;

public interface TryVariableCatchInterface<X extends Z, Z extends Throwable> {

  public TryVariableCatchInterface< ?, Z>
      andConsume(TConsumer< ? extends X> consumer);

}