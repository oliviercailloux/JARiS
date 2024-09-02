package io.github.oliviercailloux.jaris.collections;

import static com.google.common.base.Verify.verify;

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
    private final int id;

    public InternalException(Exception e, int id) {
      super(e);
      this.id = id;
    }

    /**
     * Guaranteed to be an X, if only X’s are given to the constructor.
     */
    @Override
    public synchronized Exception getCause() {
      return (Exception) super.getCause();
    }

    public int getId() {
      return id;
    }
  }

  /**
   * Wraps any checked exceptions into an InternalException with the checked exception as its cause.
   */
  private static final Unchecker<Exception, InternalException> UNCHECKER0 =
      Unchecker.wrappingWith(e -> new InternalException(e, 0));

  private static final Unchecker<Exception, InternalException> UNCHECKER1 =
      Unchecker.wrappingWith(e -> new InternalException(e, 1));

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
    final Function<? super K, V> wrapped = UNCHECKER0.wrapFunction(valueFunction);
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
   * provided the given function maps different keys to different new keys (i.e., is injective). If
   * the provided function maps two keys of the original map to two equal new keys, an
   * {@link IllegalArgumentException} is thrown.
   *
   * @param <K> the original key type
   * @param <L> the new key type
   * @param <V> the value type
   * @param <X> the type of exception that the provided function may throw (besides
   *        {@link RuntimeException} instances)
   * @param map the original map
   * @param keyTransformer an injective function that transforms keys
   * @return the corresponding map
   * @throws X if the provided function throws while transforming keys
   * @throws IllegalArgumentException if the provided function maps two keys to the same new key (as
   *         determined by {@link #equals(Object)})
   */
  public static <K, L, V, X extends Exception> ImmutableMap<L, V> transformKeys(Map<K, V> map,
      TFunction<? super K, L, X> keyTransformer) throws X {
    final Function<? super K, L> behavedKeyTransformer = UNCHECKER0.wrapFunction(keyTransformer);
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

  public static interface ValueTransformer<K, L, V, W, X extends Exception> {
    public W transform(K oldKey, L newKey, V oldValue) throws X;
  }

  private static record Triple<K, L, V> (K oldKey, L newKey, V oldValue) {
  }

  /**
   * Returns an immutable map containing as many entries as the provided one, with keys and values
   * transformed, provided the given key function maps different keys to different new keys (i.e.,
   * is injective). If the provided key function maps two keys of the original map to two equal new
   * keys, an {@link IllegalArgumentException} is thrown.
   *
   * @param <K> the original key type
   * @param <L> the new key type
   * @param <V> the value type
   * @param <X> the type of exception that the provided key function may throw (besides
   *        {@link RuntimeException} instances)
   * @param <Y> the type of exception that the provided value transformer may throw (besides
   *        {@link RuntimeException} instances)
   * @param map the original map
   * @param keyTransformer an injective function that transforms keys
   * @param valueTransformer a function that transforms values
   * @return a map of the same size than the provided map
   * @throws X if the provided key function throws while transforming keys
   * @throws Y if the provided value function throws while transforming values
   * @throws IllegalArgumentException if the provided function maps two keys to the same new key (as
   *         determined by {@link #equals(Object)})
   */
  public static <K, L, V, W, X extends Exception, Y extends Exception> ImmutableMap<L, W>
      transformKeysAndValues(Map<K, V> map, TFunction<? super K, L, X> keyTransformer,
          ValueTransformer<? super K, ? super L, ? super V, W, Y> valueTransformer) throws X, Y {
    final Function<Map.Entry<K, V>, L> behavedKeyTransformer =
        UNCHECKER0.wrapFunction(e -> keyTransformer.apply(e.getKey()));
    final Function<Triple<K, L, V>, W> behavedValueTransformer =
        UNCHECKER1.wrapFunction(t -> valueTransformer.transform(t.oldKey, t.newKey, t.oldValue));
    @SuppressWarnings("unused")
    final Function<Entry<K, V>, Triple<K, L, V>> oldEntryToTriple =
        e -> new Triple<K, L, V>(e.getKey(), behavedKeyTransformer.apply(e), e.getValue());
    final Collector<Triple<K, L, V>, ?, ImmutableMap<L, W>> collector =
        ImmutableMap.toImmutableMap(Triple::newKey, behavedValueTransformer);
    final ImmutableMap<L, W> collected;
    try {
      collected = map.entrySet().stream().map(oldEntryToTriple).collect(collector);
    } catch (InternalException e) {
      if (e.getId() == 0) {
        @SuppressWarnings("unchecked")
        final X cause = (X) e.getCause();
        throw cause;
      }
      verify(e.getId() == 1);
      @SuppressWarnings("unchecked")
      final Y cause = (Y) e.getCause();
      throw cause;
    }
    return collected;
  }

  private CollectionUtils() {
    /* No need to instantiate. */
  }
}
