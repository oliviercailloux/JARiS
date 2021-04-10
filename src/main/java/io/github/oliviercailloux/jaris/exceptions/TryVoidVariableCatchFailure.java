package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.function.Function;

abstract class TryVoidVariableCatchFailure<X extends Throwable, Z extends Throwable>
    extends TryVoidVariableCatch<X, Z> {

  protected final X cause;

  protected TryVoidVariableCatchFailure(X cause) {
    this.cause = checkNotNull(cause);
  }

  @Override
  Optional<Object> getResult() {
    return Optional.empty();
  }

  @Override
  Optional<X> getCause() {
    return Optional.of(cause);
  }

  @Override
  public <T, Y extends Exception> T map(Throwing.Supplier<? extends T, ? extends Y> supplier,
      Throwing.Function<? super X, ? extends T, ? extends Y> causeTransformation) throws Y {
    return causeTransformation.apply(cause);
  }

  @Override
  public <Y extends Exception> void ifFailed(Throwing.Consumer<? super X, Y> consumer) throws Y {
    consumer.accept(cause);
  }

  @Override
  public <Y extends Z> void orThrow(Function<X, Y> causeTransformation) throws Y {
    throw causeTransformation.apply(cause);
  }
}
