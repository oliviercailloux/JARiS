package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.Throwing.BiFunction;

public class TryFailure<X extends Exception> extends TryVariableCatchFailure<X, Exception>
    implements Try<Object, X> {
  public static <T, X extends Exception> Try<T, X> given(X cause) {
    return new TryFailure<>(cause).cast();
  }

  private TryFailure(X cause) {
    super(cause);
  }

  @Override
  boolean catchesAll() {
    return false;
  }

  private <U> Try<U, X> cast() {
    @SuppressWarnings("unchecked")
    final Try<U, X> casted = (Try<U, X>) this;
    return casted;
  }

  @Override
  public <Y extends Exception, Z extends Exception, W extends Exception> Try<Object, Z> or(
      Throwing.Supplier<? extends Object, Y> supplier,
      Throwing.BiFunction<? super X, ? super Y, ? extends Z, W> exceptionsMerger) throws W {
    final Try<Object, Y> t2 = Try.get(supplier);
    return t2.map(Try::success, y -> Try.failure(exceptionsMerger.apply(cause, y)));
  }

  @Override
  public Try<Object, X> andRun(Throwing.Runnable<? extends X> runnable) {
    return this;
  }

  @Override
  public Try<Object, X> andConsume(Throwing.Consumer<? super Object, ? extends X> consumer) {
    return this;
  }

  @Override
  public <U, V, Y extends Exception> Try<V, X> and(Try<U, ? extends X> t2,
      BiFunction<? super Object, ? super U, ? extends V, Y> merger) throws Y {
    return cast();
  }

  @Override
  public <U> Try<U, X> flatMap(Throwing.Function<? super Object, ? extends U, ? extends X> mapper) {
    return cast();
  }

}
