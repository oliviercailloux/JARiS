package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

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
	 * Wraps any checked exceptions into an InternalException with the checked
	 * exception as its cause.
	 */
	private static final Unchecker<Exception, InternalException> UNCHECKER = Unchecker
			.wrappingWith(InternalException::new);

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

	public <R, A> R collect​(Collector<? super T, A, R> collector) throws X {
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
		/**
		 * Any checked exception thrown by predicate is supposed to extend X, by its
		 * header. Only such exceptions are wrapped into an InternalException instance
		 * by the UNCHECKER. Thus, any InternalException thrown by the wrapped predicate
		 * has a Y as its cause.
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
}
