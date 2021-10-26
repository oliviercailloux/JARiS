package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.VerifyException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * <p>
 * An object able to transform functional interfaces that throw checked exceptions into functional
 * interfaces that throw only runtime exceptions; and able to invoke functional interfaces that
 * throw checked exceptions.
 * </p>
 * <p>
 * Instances of this class hold a function that transforms any checked exception to an unchecked
 * exception.
 * </p>
 * <p>
 * Instances of this class are immutable.
 * </p>
 * <p>
 * Heavily inspired by the <a href="https://github.com/diffplug/durian">durian</a> library.
 * </p>
 *
 * @param <X> the type of checked exception that this object accepts; <i>must</i> be a checked
 *        exception type (may not extend {@link RuntimeException}), otherwise the wrapper will never
 *        be used (and hence such an object has no use)
 * @param <Y> the type of unchecked exception that this object throws in place of the checked
 *        exception
 */
public class Unchecker<X extends Exception, Y extends RuntimeException> {
  /**
   * An object that accepts functional interfaces that throw {@link IOException} instances; and that
   * will throw {@link UncheckedIOException} instances instead.
   */
  public static final Unchecker<IOException, UncheckedIOException> IO_UNCHECKER =
      Unchecker.wrappingWith(UncheckedIOException::new);

  /**
   * An object that accepts functional interfaces that throw {@link URISyntaxException} instances;
   * and that will throw {@link VerifyException} instances instead.
   */
  public static final Unchecker<URISyntaxException, VerifyException> URI_UNCHECKER =
      Unchecker.wrappingWith(VerifyException::new);

  /**
   * Returns an object that will use the given wrapper function to transform checked exceptions to
   * unchecked ones, if any checked exception happens.
   *
   * @param <X> the type of checked exception that the returned instance accepts
   * @param <Y> the type of unchecked exception that the returned instance throws in place of the
   *        checked exception
   * @param wrapper the function used to transform checked expections to unchecked ones
   * @return an unchecker instance
   */
  public static <X extends Exception, Y extends RuntimeException> Unchecker<X, Y>
      wrappingWith(Function<X, Y> wrapper) {
    return new Unchecker<>(wrapper);
  }

  private final Function<X, Y> wrapper;

  private Unchecker(Function<X, Y> wrapper) {
    this.wrapper = checkNotNull(wrapper);
  }

  /**
   * Calls the given runnable; if it throws a checked exception, throws an unchecked exception
   * instead, applying the wrapper; if the runnable throws an unchecked exception, the exception is
   * thrown unchanged.
   *
   * @param runnable the runnable to call
   * @throws Y iff the runnable throws a checked exception (or an unchecked exception of type Y)
   */
  public void call(Throwing.Runnable<X> runnable) throws Y {
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

  /**
   * Attempts to get and return a result from the given supplier; if the supplier throws a checked
   * exception, throws an unchecked exception instead, applying the wrapper; if the supplier throws
   * an unchecked exception, the exception is thrown unchanged.
   *
   * @param <T> the type returned by the supplier
   * @param supplier the supplier to invoke
   * @return the result obtained from the supplier
   * @throws Y iff the supplier throws a checked exception (or an unchecked exception of type Y)
   */
  public <T> T getUsing(Throwing.Supplier<T, ? extends X> supplier) throws Y {
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

  /**
   * Returns a runnable that delegates to the given runnable, except that any checked exception
   * thrown by the given runnable is instead thrown by the returned runnable as an unchecked
   * exception, applying the wrapper to transform it.
   *
   * @param runnable the instance that is delegated to
   * @return a delegating runnable
   */
  public Runnable wrapRunnable(Throwing.Runnable<? extends X> runnable) {
    return () -> {
      try {
        runnable.run();
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        @SuppressWarnings("unchecked")
        final X ef = (X) e;
        throw wrapper.apply(ef);
      }
    };
  }

  /**
   * Returns a supplier that simply delegates to the given supplier, except that any checked
   * exception thrown by the given supplier is instead thrown by the returned supplier as an
   * unchecked exception, applying the wrapper to transform it.
   *
   * @param <T> the type that the resulting instance will supply
   * @param supplier the instance that is delegated to
   * @return a delegating supplier
   */
  public <T> Supplier<T> wrapSupplier(Throwing.Supplier<? extends T, ? extends X> supplier) {
    return () -> {
      try {
        return supplier.get();
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        @SuppressWarnings("unchecked")
        final X ef = (X) e;
        throw wrapper.apply(ef);
      }
    };
  }

  /**
   * Returns a comparator that simply delegates to the given comparator, except that any checked
   * exception thrown by the given comparator is instead thrown by the returned comparator as an
   * unchecked exception, applying the wrapper to transform it.
   *
   * @param <T> the type of objects that the returned comparator may compare
   * @param comparator the instance that is delegated to
   * @return a delegating comparator
   */
  public <T> Comparator<T> wrapComparator(Throwing.Comparator<? super T, ? extends X> comparator) {
    return (t1, t2) -> {
      try {
        return comparator.compare(t1, t2);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        @SuppressWarnings("unchecked")
        final X ef = (X) e;
        throw wrapper.apply(ef);
      }
    };
  }

  /**
   * Returns a consumer that simply delegates to the given consumer, except that any checked
   * exception thrown by the given consumer is instead thrown by the returned consumer as an
   * unchecked exception, applying the wrapper to transform it.
   *
   * @param <T> the type of objects that the resulting instance may consume
   * @param consumer the instance that is delegated to
   * @return a delegating consumer
   */
  public <T> Consumer<T> wrapConsumer(Throwing.Consumer<? super T, ? extends X> consumer) {
    return t -> {
      try {
        consumer.accept(t);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        @SuppressWarnings("unchecked")
        final X ef = (X) e;
        throw wrapper.apply(ef);
      }
    };
  }

  /**
   * Returns a bi consumer that simply delegates to the given bi consumer, except that any checked
   * exception thrown by the given consumer is instead thrown by the returned consumer as an
   * unchecked exception, applying the wrapper to transform it.
   *
   * @param <T> the “left” type of objects that the resulting instance may consume
   * @param <U> the “right” type of objects that the resulting instance may consume
   * @param consumer the instance that is delegated to
   * @return a delegating bi consumer
   */
  public <T, U> BiConsumer<T, U>
      wrapBiConsumer(Throwing.BiConsumer<? super T, ? super U, ? extends X> consumer) {
    return (t, u) -> {
      try {
        consumer.accept(t, u);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        @SuppressWarnings("unchecked")
        final X ef = (X) e;
        throw wrapper.apply(ef);
      }
    };
  }

  /**
   * Returns a function that simply delegates to the given function, except that any checked
   * exception thrown by the given function is instead thrown by the returned function as an
   * unchecked exception, applying the wrapper to transform it.
   *
   * @param <F> the type of objects that the resulting function accepts
   * @param <T> the type of objects that the resulting function produces
   * @param function the instance that is delegated to
   * @return a delegating function
   */
  public <F, T> Function<F, T>
      wrapFunction(Throwing.Function<? super F, ? extends T, ? extends X> function) {
    return arg -> {
      try {
        return function.apply(arg);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        @SuppressWarnings("unchecked")
        final X ef = (X) e;
        throw wrapper.apply(ef);
      }
    };
  }

  /**
   * Returns a bi function that simply delegates to the given bi function, except that any checked
   * exception thrown by the given bi function is instead thrown by the returned bi function as an
   * unchecked exception, applying the wrapper to transform it.
   *
   * @param <F1> the “left” type of objects that the resulting function accepts
   * @param <F2> the “right” type of objects that the resulting function accepts
   * @param <T> the type of objects that the resulting function produces
   * @param function the instance that is delegated to
   * @return a delegating bi function
   */
  public <F1, F2, T> BiFunction<F1, F2, T> wrapBiFunction(
      Throwing.BiFunction<? super F1, ? super F2, ? extends T, ? extends X> function) {
    return (t, u) -> {
      try {
        return function.apply(t, u);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        @SuppressWarnings("unchecked")
        final X ef = (X) e;
        throw wrapper.apply(ef);
      }
    };
  }

  /**
   * Returns a binary operator that simply delegates to the given binary operator, except that any
   * checked exception thrown by the given binary operator is instead thrown by the returned binary
   * operator as an unchecked exception, applying the wrapper to transform it.
   *
   * @param <F> the type of objects that the resulting binary operator works on
   * @param operator the instance that is delegated to
   * @return a delegating binary operator
   */
  public <F> BinaryOperator<F>
      wrapBinaryOperator(Throwing.BinaryOperator<F, ? extends X> operator) {
    return (t, u) -> {
      try {
        return operator.apply(t, u);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        @SuppressWarnings("unchecked")
        final X ef = (X) e;
        throw wrapper.apply(ef);
      }
    };
  }

  /**
   * Returns a predicate that simply delegates to the given predicate, except that any checked
   * exception thrown by the given predicate is instead thrown by the returned predicate as an
   * unchecked exception, applying the wrapper to transform it.
   *
   * @param <F> the type of objects that the resulting predicate accepts
   * @param predicate the instance that is delegated to
   * @return a delegating predicate
   */
  public <F> Predicate<F> wrapPredicate(Throwing.Predicate<? super F, ? extends X> predicate) {
    return (arg) -> {
      try {
        return predicate.test(arg);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        @SuppressWarnings("unchecked")
        final X ef = (X) e;
        throw wrapper.apply(ef);
      }
    };
  }
}
