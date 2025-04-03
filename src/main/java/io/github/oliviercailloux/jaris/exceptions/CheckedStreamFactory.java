package io.github.oliviercailloux.jaris.exceptions;

import java.util.Collection;
import java.util.stream.Stream;

public class CheckedStreamFactory<X extends Exception> {
  static <X extends Exception> CheckedStreamFactory<X> instance() {
    return new CheckedStreamFactory<>();
  }

  private CheckedStreamFactory() {
    // Private constructor to prevent instantiation
  }

  public <T> CheckedStream<T, X> wrapping(Stream<T> delegate) {
    return CheckedStreamImpl.wrapping(delegate);
  }
  public <T> CheckedStream<T, X> from(Collection<T> collection) {
    return wrapping(collection.stream());
  }
  
}
