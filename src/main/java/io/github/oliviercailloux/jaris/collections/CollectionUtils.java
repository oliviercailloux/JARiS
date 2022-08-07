package io.github.oliviercailloux.jaris.collections;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.oliviercailloux.jaris.exceptions.Unchecker;
import io.github.oliviercailloux.jaris.throwing.TFunction;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * A few helper methods generally useful when dealing with collections, felt to miss from the JDK
 * and Guava.
 */
public class CollectionUtils {
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

  /**
   * Returns an immutable map with the given {@code keys} and whose value for each key was computed
   * by {@code valueFunction}. The map’s iteration order is the order of {@code keys}.
   *
   * @param <K> the key type of the provided and of the returned map
   * @param <V> the value type of the returned map
   * @param <X> the type of exception that the provided function may throw
   * @param keys the keys to use as the map keys
   * @param valueFunction the function producing the values
   * @return an immutable map
   * @throws X if the given function throws an exception while computing values
   */
  public static <K, V, X extends Exception> ImmutableMap<K, V> toMap(Set<K> keys,
      TFunction<? super K, V, X> valueFunction) throws X {
    final Function<? super K, V> wrapped = UNCHECKER.wrapFunction(valueFunction);
    try {
      return Maps.toMap(keys, wrapped::apply);
    } catch (InternalException e) {
      @SuppressWarnings("unchecked")
      final X cause = (X) e.getCause();
      throw cause;
    }
  }

  /**
   * Returns an immutable map containing as many entries as the provided one, with keys transformed,
   * unless the provided function maps two keys of the original map to two equal new keys, in which
   * case an {@link IllegalArgumentException} is thrown.
   *
   * @param <K> the original key type
   * @param <L> the new key type
   * @param <V> the value type
   * @param <X> the type of exception that the provided function may throw (besides
   *        {@link RuntimeException} instances)
   * @param map the original map
   * @param keyTransformer the function that transforms keys
   * @return the corresponding map
   * @throws X if the provided function throws while transforming keys
   * @throws IllegalArgumentException if the provided function maps two keys to the same new key (as
   *         determined by {@link #equals(Object)})
   */
  public static <K, L, V, X extends Exception> ImmutableMap<L, V> transformKeys(Map<K, V> map,
      TFunction<? super K, L, X> keyTransformer) throws X {
    final Function<? super K, L> behavedKeyTransformer = UNCHECKER.wrapFunction(keyTransformer);
    final Collector<Entry<K, V>, ?, ImmutableMap<L, V>> collector =
        ImmutableMap.toImmutableMap(e -> behavedKeyTransformer.apply(e.getKey()), Entry::getValue);
    final ImmutableMap<L, V> collected;
    try {
      collected = map.entrySet().stream().collect(collector);
    } catch (InternalException e) {
      @SuppressWarnings("unchecked")
      final X cause = (X) e.getCause();
      throw cause;
    }
    return collected;
  }

  private CollectionUtils() {
    /* No need to instantiate. */
  }
}
