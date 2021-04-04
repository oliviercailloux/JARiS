package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Function;
import java.util.Optional;

abstract class TryVariableCatchSuccess<T, X extends Throwable> extends TryVariableCatch<T, X>
    implements TryVariableCatchInterface<T, X> {

  private final T result;

  private TryVariableCatchSuccess(T result) {
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

  private <Y extends TE> TrySingleImpl<T, Y, TE> cast() {
    @SuppressWarnings("unchecked")
    final TrySingleImpl<T, Y, TE> casted = (TrySingleImpl<T, Y, TE>) this;
    return casted;
  }

  @Override
  public <U, Y extends Exception> U map(
      Throwing.Function<? super T, ? extends U, ? extends Y> transformation,
      Throwing.Function<? super X, ? extends U, ? extends Y> causeTransformation) throws Y {
    return transformation.apply(result);
  }

  @Override
  public <Y extends Exception> T orMapCause(Function<? super X, ? extends T, Y> causeTransformation)
      throws Y {
    return result;
  }

  @Override
  public <Y extends Exception> Optional<T> orConsumeCause(Consumer<? super X, Y> consumer)
      throws Y {
    return Optional.of(result);
  }

  @Override
  public T orThrow() throws X {
    return result;
  }

  @Override
  public <Y extends Throwable, Z extends Throwable, W extends Exception> TryVariableCatchInterface<T, Z> or(
      Throwing.Supplier<? extends T, Y> supplier,
      Throwing.BiFunction<? super X, ? super Y, ? extends Z, W> exceptionsMerger) throws W {
    return cast();
  }
}
