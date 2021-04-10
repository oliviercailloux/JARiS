package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.function.Function;

abstract class TryVariableCatchSuccess<T, X extends Throwable, Z extends Throwable>
    extends TryVariableCatch<T, X, Z> {

  protected final T result;

  protected TryVariableCatchSuccess(T result) {
    this.result = checkNotNull(result);
  }

  @Override
  Optional<T> getResult() {
    return Optional.of(result);
  }

  @Override
  Optional<X> getCause() {
    return Optional.empty();
  }

  @Override
  public <U, Y extends Exception> U map(
      Throwing.Function<? super T, ? extends U, ? extends Y> transformation,
      Throwing.Function<? super X, ? extends U, ? extends Y> causeTransformation) throws Y {
    return transformation.apply(result);
  }

  @Override
  public <Y extends Exception> T orMapCause(
      Throwing.Function<? super X, ? extends T, Y> causeTransformation) throws Y {
    return result;
  }

  @Override
  public <Y extends Exception> Optional<T> orConsumeCause(Throwing.Consumer<? super X, Y> consumer)
      throws Y {
    return Optional.of(result);
  }

  @Override
  public <Y extends Z> T orThrow(Function<X, Y> causeTransformation) {
    return result;
  }
}
