package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.function.Function;

abstract class TryVariableCatchFailure<X extends Throwable, Z extends Throwable>
    extends TryVariableCatch<Object, X, Z> implements TryVariableCatchInterface<Object, X, Z> {

  protected final X cause;

  protected TryVariableCatchFailure(X cause) {
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
  public <U, Y extends Exception> U map(
      Throwing.Function<? super Object, ? extends U, ? extends Y> transformation,
      Throwing.Function<? super X, ? extends U, ? extends Y> causeTransformation) throws Y {
    return causeTransformation.apply(cause);
  }

  @Override
  public <Y extends Exception> Object orMapCause(
      Throwing.Function<? super X, ? extends Object, Y> causeTransformation) throws Y {
    return causeTransformation.apply(cause);
  }

  @Override
  public <Y extends Exception> Optional<Object> orConsumeCause(
      Throwing.Consumer<? super X, Y> consumer) throws Y {
    consumer.accept(cause);
    return Optional.empty();
  }

  @Override
  public <Y extends Z> Object orThrow(Function<X, Y> causeTransformation) throws Y {
    throw causeTransformation.apply(cause);
  }
}
