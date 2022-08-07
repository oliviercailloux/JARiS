package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import io.github.oliviercailloux.jaris.throwing.TBiConsumer;
import io.github.oliviercailloux.jaris.throwing.TBiFunction;
import io.github.oliviercailloux.jaris.throwing.TBinaryOperator;
import io.github.oliviercailloux.jaris.throwing.TComparator;
import io.github.oliviercailloux.jaris.throwing.TConsumer;
import io.github.oliviercailloux.jaris.throwing.TFunction;
import io.github.oliviercailloux.jaris.throwing.TPredicate;
import io.github.oliviercailloux.jaris.throwing.TSupplier;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

class CheckedStreamImpl<T, X extends Exception> implements CheckedStream<T, X> {
  @SuppressWarnings("serial")
  private static class InternalException extends RuntimeException {
    public InternalException(Exception e) {
      super(e);
    }

    /**
     * Guaranteed to be an X, if only X’s are given to the constructor.
     */
    @Override
    public synchronized Exception getCause() {
      return (Exception) super.getCause();
    }
  }

  /**
   * Wraps any checked exceptions into an InternalException with the checked exception as its cause.
   */
  private static final Unchecker<Exception, InternalException> UNCHECKER =
      Unchecker.wrappingWith(InternalException::new);

  public static <T, X extends Exception> CheckedStreamImpl<T, X> wrapping(Stream<T> delegate) {
    return new CheckedStreamImpl<>(delegate);
  }

  public static <T, X extends Exception> CheckedStreamImpl<T, X>
      generate(TSupplier<? extends T, ? extends X> generator) {
    final Supplier<? extends T> wrapped = UNCHECKER.wrapSupplier(generator);
    return new CheckedStreamImpl<>(Stream.generate(wrapped));
  }

  private final Stream<T> delegate;

  private CheckedStreamImpl(Stream<T> delegate) {
    this.delegate = checkNotNull(delegate);
  }

  @Override
  public CheckedStreamImpl<T, X> distinct() {
    return new CheckedStreamImpl<>(delegate.distinct());
  }

  @Override
  public CheckedStreamImpl<T, X> dropWhile(TPredicate<? super T, ? extends X> predicate) {
    final Predicate<? super T> wrapped = UNCHECKER.wrapPredicate(predicate);
    return new CheckedStreamImpl<>(delegate.dropWhile(wrapped));
  }

  @Override
  public CheckedStreamImpl<T, X> takeWhile(TPredicate<? super T, ? extends X> predicate) {
    final Predicate<? super T> wrapped = UNCHECKER.wrapPredicate(predicate);
    return new CheckedStreamImpl<>(delegate.takeWhile(wrapped));
  }

  @Override
  public CheckedStreamImpl<T, X> filter(TPredicate<? super T, ? extends X> predicate) {
    final Predicate<? super T> wrapped = UNCHECKER.wrapPredicate(predicate);
    return new CheckedStreamImpl<>(delegate.filter(wrapped));
  }

  @Override
  public <R> CheckedStreamImpl<R, X>
      flatMap(TFunction<? super T, ? extends Stream<? extends R>, ? extends X> mapper) {
    final Function<? super T, ? extends Stream<? extends R>> wrapped =
        UNCHECKER.wrapFunction(mapper);
    return new CheckedStreamImpl<>(delegate.flatMap(wrapped));
  }

  @Override
  public CheckedStreamImpl<T, X> limit(long maxSize) {
    return new CheckedStreamImpl<>(delegate.limit(maxSize));
  }

  @Override
  public <R> CheckedStreamImpl<R, X>
      map(TFunction<? super T, ? extends R, ? extends X> mapper) {
    final Function<? super T, ? extends R> wrapped = UNCHECKER.wrapFunction(mapper);
    return new CheckedStreamImpl<>(delegate.map(wrapped));
  }

  @Override
  public CheckedStreamImpl<T, X> skip(long n) {
    return new CheckedStreamImpl<>(delegate.skip(n));
  }

  @Override
  public CheckedStreamImpl<T, X> sorted() {
    return new CheckedStreamImpl<>(delegate.sorted());
  }

  @Override
  public CheckedStreamImpl<T, X> sorted(TComparator<? super T, ? extends X> comparator) {
    final Comparator<? super T> wrapped = UNCHECKER.wrapComparator(comparator);
    return new CheckedStreamImpl<>(delegate.sorted(wrapped));
  }

  @Override
  public T reduce(T identity, TBinaryOperator<T, ? extends X> accumulator) throws X {
    final BinaryOperator<T> wrapped = UNCHECKER.wrapBinaryOperator(accumulator);
    try {
      return delegate.reduce(identity, wrapped);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public Optional<T> reduce(TBinaryOperator<T, ? extends X> accumulator) throws X {
    final BinaryOperator<T> wrapped = UNCHECKER.wrapBinaryOperator(accumulator);
    try {
      return delegate.reduce(wrapped);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public <U> U reduce(U identity, TBiFunction<U, ? super T, U, ? extends X> accumulator,
      TBinaryOperator<U, ? extends X> combiner) throws X {
    final BiFunction<U, ? super T, U> wrappedAccumulator = UNCHECKER.wrapBiFunction(accumulator);
    final BinaryOperator<U> wrappedCombiner = UNCHECKER.wrapBinaryOperator(combiner);
    try {
      return delegate.reduce(identity, wrappedAccumulator, wrappedCombiner);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public <R> R collect(TSupplier<R, ? extends X> supplier,
      TBiConsumer<R, ? super T, ? extends X> accumulator,
      TBiConsumer<R, R, ? extends X> combiner) throws X {
    final Supplier<R> wrappedSupplier = UNCHECKER.wrapSupplier(supplier);
    final BiConsumer<R, ? super T> wrappedAccumulator = UNCHECKER.wrapBiConsumer(accumulator);
    final BiConsumer<R, R> wrappedCombiner = UNCHECKER.wrapBiConsumer(combiner);
    try {
      return delegate.collect(wrappedSupplier, wrappedAccumulator, wrappedCombiner);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public <R, A> R collect(Collector<? super T, A, R> collector) throws X {
    try {
      return delegate.collect(collector);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  /**
   * Returns whether all elements of this stream match the provided predicate. May not evaluate the
   * predicate on all elements if not necessary for determining the result. If the stream is empty
   * then {@code true} is returned and the predicate is not evaluated.
   *
   * <p>
   * This is a <a href="package-summary.html#StreamOps">short-circuiting terminal operation</a>.
   *
   * @apiNote This method evaluates the <em>universal quantification</em> of the predicate over the
   *          elements of the stream (for all x P(x)). If the stream is empty, the quantification is
   *          said to be <em>vacuously satisfied</em> and is always {@code true} (regardless of
   *          P(x)).
   *
   * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
   *        <a href="package-summary.html#Statelessness">stateless</a> predicate to apply to
   *        elements of this stream
   * @return {@code true} if either all elements of the stream match the provided predicate or the
   *         stream is empty, otherwise {@code false}
   * @throws X if any functional interface operating on this stream throws a checked exception
   * @see Stream#allMatch(Predicate)
   */
  @Override
  public boolean allMatch(TPredicate<? super T, ? extends X> predicate) throws X {
    /*
     * Any checked exception thrown by predicate is supposed to extend X, by its header. Only such
     * exceptions are wrapped into an InternalException instance by the UNCHECKER. Thus, any
     * InternalException thrown by the wrapped predicate has a Y as its cause.
     */
    final Predicate<? super T> wrapped = UNCHECKER.wrapPredicate(predicate);
    try {
      return delegate.allMatch(wrapped);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public boolean anyMatch(TPredicate<? super T, ? extends X> predicate) throws X {
    final Predicate<? super T> wrapped = UNCHECKER.wrapPredicate(predicate);
    try {
      return delegate.anyMatch(wrapped);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public boolean noneMatch(TPredicate<? super T, ? extends X> predicate) throws X {
    final Predicate<? super T> wrapped = UNCHECKER.wrapPredicate(predicate);
    try {
      return delegate.noneMatch(wrapped);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public CheckedStreamImpl<T, X> peek(TConsumer<? super T, ? extends X> action) throws X {
    final Consumer<? super T> wrapped = UNCHECKER.wrapConsumer(action);
    try {
      return new CheckedStreamImpl<>(delegate.peek(wrapped));
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public long count() throws X {
    try {
      return delegate.count();
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public Optional<T> findAny() throws X {
    try {
      return delegate.findAny();
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public Optional<T> findFirst() throws X {
    try {
      return delegate.findFirst();
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public void forEach(TConsumer<? super T, ? extends X> action) throws X {
    final Consumer<? super T> wrapped = UNCHECKER.wrapConsumer(action);
    try {
      delegate.forEach(wrapped);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public void forEachOrdered(TConsumer<? super T, ? extends X> action) throws X {
    final Consumer<? super T> wrapped = UNCHECKER.wrapConsumer(action);
    try {
      delegate.forEachOrdered(wrapped);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public Optional<T> max(TComparator<? super T, ? extends X> comparator) throws X {
    final Comparator<? super T> wrapped = UNCHECKER.wrapComparator(comparator);
    try {
      return delegate.max(wrapped);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public Optional<T> min(TComparator<? super T, ? extends X> comparator) throws X {
    final Comparator<? super T> wrapped = UNCHECKER.wrapComparator(comparator);
    try {
      return delegate.min(wrapped);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }

  @Override
  public ImmutableList<T> toList() throws X {
    try {
      return delegate.collect(ImmutableList.toImmutableList());
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }
}
