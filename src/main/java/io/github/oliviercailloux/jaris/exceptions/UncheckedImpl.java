package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.oliviercailloux.jaris.throwing.TRunnable;
import io.github.oliviercailloux.jaris.throwing.TSupplier;
import java.util.function.Function;

class UncheckedImpl<X extends Exception, Y extends Exception> implements Unchecked<X, Y> {

  protected final Function<X, Y> wrapper;

  protected UncheckedImpl(Function<X, Y> wrapper) {
    this.wrapper = checkNotNull(wrapper);
  }

  @Override
  public void call(TRunnable<? extends X> runnable) throws Y {
    try {
      runnable.run();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      @SuppressWarnings("unchecked")
      final X ef = (X) e;
      throw wrapper.apply(ef);
    }
  }

  @Override
  public <T> T getUsing(TSupplier<? extends T, ? extends X> supplier) throws Y {
    try {
      return supplier.get();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      @SuppressWarnings("unchecked")
      final X ef = (X) e;
      throw wrapper.apply(ef);
    }
  }
}
