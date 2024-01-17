package io.github.oliviercailloux.jaris.throwing;

import static com.google.common.base.Preconditions.checkNotNull;

public interface TConsumer<T, X extends Throwable> {
  public void consuming(T t) throws X;
}
