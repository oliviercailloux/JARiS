package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Function;
import java.util.Optional;

abstract class TryVariableCatchFailure<T, X extends Throwable> extends TryVariableCatch<T, X>
    implements TryVariableCatchInterface<T, X> {

  private final X cause;

  private TryVariableCatchFailure(X cause) {
    this.cause = checkNotNull(cause);
  }

  @Override
  Optional<T> getResult() {
    return Optional.empty();
  }

  @Override
  Optional<X> getCause() {
    return Optional.of(cause);
  }

  private <U> TrySingleImpl<U, X, TE> cast() {
    @SuppressWarnings("unchecked")
    final TrySingleImpl<U, X, TE> casted = (TrySingleImpl<U, X, TE>) this;
    return casted;
  }

  @Override
  public <U, Y extends Exception> U map(
      Throwing.Function<? super T, ? extends U, ? extends Y> transformation,
      Throwing.Function<? super X, ? extends U, ? extends Y> causeTransformation) throws Y {
    return causeTransformation.apply(cause);
  }

  @Override
  public <Y extends Exception> T orMapCause(Function<? super X, ? extends T, Y> causeTransformation)
      throws Y {
    return causeTransformation.apply(cause);
  }

  @Override
  public <Y extends Exception> Optional<T> orConsumeCause(Consumer<? super X, Y> consumer)
      throws Y {
    consumer.accept(cause);
    return Optional.empty();
  }

  @Override
  public T orThrow() throws X {
    throw cause;
  }

  @Override
  public TryVariableCatchInterface<T, X> andRun(Throwing.Runnable<? extends X> runnable) {
    return this;
  }

  @Override
  public TryVariableCatchInterface<T, X> andConsume(
      Throwing.Consumer<? super T, ? extends X> consumer) {
    return this;
  }

  @Override
  public <U, V, Y extends Exception> TryVariableCatchInterface<V, X> and(
      TryVariableCatchInterface<U, ? extends X> t2,
      Throwing.BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y {
    return cast();
  }

  @Override
  public <U> TrySingleImpl<U, X, TE> flatMap(Function<? super T, ? extends U, ? extends X> mapper) {
    return cast();
  }
}
