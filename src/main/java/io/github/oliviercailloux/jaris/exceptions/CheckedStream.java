package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 *
 * Heavily inspired by
 * <a href= "https://github.com/JeffreyFalgout/ThrowingStream/">ThrowingStream</a>.
 *
 * @param <T>
 * @param <X>
 */
public class CheckedStream<T, X extends Exception> {
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

  public static <E, X extends Exception> CheckedStream<E, X> wrapping(Stream<E> delegate) {
    return new CheckedStream<>(delegate);
  }

  private final Stream<T> delegate;

  private CheckedStream(Stream<T> delegate) {
    this.delegate = checkNotNull(delegate);
  }

  public CheckedStream<T, X> distinct() {
    return new CheckedStream<>(delegate.distinct());
  }

  public CheckedStream<T, X> dropWhile(Throwing.Predicate<? super T, ? extends X> predicate) {
    final Predicate<? super T> wrapped = UNCHECKER.wrapPredicate(predicate);
    return new CheckedStream<>(delegate.dropWhile(wrapped));
  }

  public CheckedStream<T, X> filter(Throwing.Predicate<? super T, ? extends X> predicate) {
    final Predicate<? super T> wrapped = UNCHECKER.wrapPredicate(predicate);
    return new CheckedStream<>(delegate.filter(wrapped));
  }

  public <R> CheckedStream<R, X> flatMap(
      Throwing.Function<? super T, ? extends Stream<? extends R>, ? extends X> mapper) {
    final Function<? super T, ? extends Stream<? extends R>> wrapped =
        UNCHECKER.wrapFunction(mapper);
    return new CheckedStream<>(delegate.flatMap(wrapped));
  }

  public CheckedStream<T, X> limit(long maxSize) {
    return new CheckedStream<>(delegate.limit(maxSize));
  }

  public <R> CheckedStream<R, X> map(
      Throwing.Function<? super T, ? extends R, ? extends X> mapper) {
    final Function<? super T, ? extends R> wrapped = UNCHECKER.wrapFunction(mapper);
    return new CheckedStream<>(delegate.map(wrapped));
  }

  public <R> R collect(Throwing.Supplier<R, ? extends X> supplier,
      Throwing.BiConsumer<R, ? super T, ? extends X> accumulator,
      Throwing.BiConsumer<R, R, ? extends X> combiner) throws X {
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

  public boolean allMatch(Throwing.Predicate<? super T, ? extends X> predicate) throws X {
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

  public boolean anyMatch(Throwing.Predicate<? super T, ? extends X> predicate) throws X {
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

  public void forEach(Throwing.Consumer<? super T, ? extends X> action) throws X {
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

  public void forEachOrdered(Throwing.Consumer<? super T, ? extends X> action) throws X {
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

  public Optional<T> max(Throwing.Comparator<? super T, ? extends X> comparator) throws X {
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

  public Optional<T> min(Comparator<? super T> comparator) throws X {
    try {
      return delegate.min(comparator);
    } catch (InternalException e) {
      final Exception cause = e.getCause();
      @SuppressWarnings("unchecked")
      final X castedCause = (X) cause;
      throw castedCause;
    }
  }
}
