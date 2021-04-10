package io.github.oliviercailloux.jaris.exceptions;

import io.github.oliviercailloux.jaris.exceptions.Throwing.BiFunction;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Function;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;
import io.github.oliviercailloux.jaris.exceptions.old.TryVoid;

class TrySuccess<T> extends TryVariableCatchSuccess<T, Exception, Exception>
    implements Try<T, Exception> {
  public static <T, X extends Exception> Try<T, X> given(T result) {
    return new TrySuccess<>(result).cast();
  }

  private TrySuccess(T result) {
    super(result);
  }

  @Override
  boolean catchesAll() {
    return false;
  }

  private <Y extends Exception> Try<T, Y> cast() {
    @SuppressWarnings("unchecked")
    final Try<T, Y> casted = (Try<T, Y>) this;
    return casted;
  }

  @Override
  public <Y extends Exception, Z extends Exception, W extends Exception> Try<T, Z> or(
      Supplier<? extends T, Y> supplier,
      BiFunction<? super Exception, ? super Y, ? extends Z, W> exceptionsMerger) throws W {
    return cast();
  }

  @Override
  public Try<T, Exception> andRun(Runnable<? extends Exception> runnable) {
    final TryVoid<? extends Exception> ran = TryVoid.run(runnable);
    return ran.map(() -> this, Try::failure);
  }

  @Override
  public Try<T, Exception> andConsume(Consumer<? super T, ? extends Exception> consumer) {
    return andRun(() -> consumer.accept(result));
  }

  @Override
  public <U, V, Y extends Exception> Try<V, Exception> and(Try<U, ? extends Exception> t2,
      BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y {
    return t2.map(u -> Try.success(merger.apply(result, u)), Try::failure);
  }

  @Override
  public <U> Try<U, Exception> flatMap(
      Function<? super T, ? extends U, ? extends Exception> mapper) {
    return Try.get(() -> mapper.apply(result));
  }
}
