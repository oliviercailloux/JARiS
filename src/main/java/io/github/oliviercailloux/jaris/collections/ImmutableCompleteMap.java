package io.github.oliviercailloux.jaris.collections;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.EnumMap;
import java.util.Map;

/**
 * An immutable map, providing all the guarantees of {@link ImmutableMap}, with the supplementary
 * guarantee of being <em>complete</em>, meaning that its key set equals all possible instances of
 * {@code K}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface ImmutableCompleteMap<K, V> extends Map<K, V> {

  /**
   * Returns an immutable map instance containing the given entries. Internally, the returned map
   * will be backed by an {@link EnumMap}.
   *
   * <p>
   * The iteration order of the returned map follows the enum's iteration order, not the order in
   * which the elements appear in the given map.
   * </p>
   *
   * @param enumType the type that is used as key
   * @param map the map to make an immutable copy of
   * @return an immutable map containing those entries
   * @throws IllegalArgumentException if the map does not contain one value for each possible value
   *         of {@code enumType}
   */
  public static <K extends Enum<K>, V> ImmutableCompleteMap<K, V> fromEnumType(Class<K> enumType,
      Map<K, V> map) {
    return EnumDelegatingImmutableCompleteMap.given(enumType, map);
  }

  /**
   * Returns {@code true} iff the key type cannot be instanciated.
   *
   * @return {@code true} iff the key type cannot be instanciated
   */
  @Override
  public boolean isEmpty();

  /**
   * Returns {@code true} iff {@code key} is of type {@code K}; otherwise, throws
   * {@link ClassCastException}.
   *
   * @return {@code true} iff {@code key} is of type {@code K}
   * @throws ClassCastException iff {@code key} is not of type {@code K}
   */
  @Override
  public boolean containsKey(Object key);

  /**
   * Returns the immutable set of all possible instances of type {@code K}, in the same order that
   * they appear in {@link #entrySet}.
   *
   * @return the set of all possible instances of type {@code K}
   */
  @Override
  public ImmutableSet<K> keySet();

  /**
   * Returns an immutable collection of the values in this map, in the same order that they appear
   * in {@link #entrySet}.
   */
  @Override
  public ImmutableCollection<V> values();

  /**
   * Returns the number of possible instances of type {@code K}.
   */
  @Override
  public int size();

  /**
   * Returns the value to which the specified key is mapped.
   *
   * @return the value to which the specified key is mapped.
   * @throws ClassCastException iff {@code key} is not of type {@code K}
   */
  @Override
  public V get(Object key);

  /**
   * Returns an immutable set of the mappings in this map. The iteration order is specified by the
   * method used to create this map. Typically, it is the natural order of the type {@code K}.
   */
  @Override
  public ImmutableSet<Entry<K, V>> entrySet();

  /**
   * Throws an exception and leaves the map unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public V put(K key, V value);

  /**
   * Throws an exception and leaves the map unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public V remove(Object key);

  /**
   * Throws an exception and leaves the map unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public void putAll(Map<? extends K, ? extends V> m);

  /**
   * Throws an exception and leaves the map unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public void clear();
}
