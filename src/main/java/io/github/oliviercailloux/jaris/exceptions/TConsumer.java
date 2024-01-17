package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

public interface TConsumer<X extends Throwable> {
  public void consuming(Integer t) throws X;
}
