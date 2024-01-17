package io.github.oliviercailloux.jaris.exceptions;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public interface MyInterface<X extends Z, Z extends Random> {

  public MyInterface<?, Random> andConsume(TConsumer<? extends X> consumer);
}
