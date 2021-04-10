package io.github.oliviercailloux.jaris.exceptions;

import java.util.Optional;
import java.util.function.Function;

abstract class TryVoidVariableCatchSuccess<X extends Throwable, Z extends Throwable>
    extends TryVoidVariableCatch<X, Z> {

  protected TryVoidVariableCatchSuccess() {
    /* Reducing visibility. */
  }

  @Override
  Optional<Object> getResult() {
    return Optional.empty();
  }

  @Override
  Optional<X> getCause() {
    return Optional.empty();
  }

  @Override
  public <T, Y extends Exception> T map(Throwing.Supplier<? extends T, ? extends Y> supplier,
      Throwing.Function<? super X, ? extends T, ? extends Y> causeTransformation) throws Y {
    return supplier.get();
  }

  @Override
  public <Y extends Exception> void ifFailed(Throwing.Consumer<? super X, Y> consumer) throws Y {
    /* Nothing to do. */
  }

  @Override
  public <Y extends Z> void orThrow(Function<X, Y> causeTransformation) {
    /* Nothing to do. */
  }


}
