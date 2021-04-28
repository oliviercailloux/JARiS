package io.github.oliviercailloux.jaris.collections;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;

/**
 * Iteration order follows the enum defined order.
 */
class EnumDelegatingImmutableCompleteMap<K extends Enum<K>, V>
    implements ImmutableCompleteMap<K, V> {
  public static <K extends Enum<K>, V> EnumDelegatingImmutableCompleteMap<K, V>
      given(Class<K> enumType, Map<K, V> values) {
    return new EnumDelegatingImmutableCompleteMap<>(enumType, values);
  }

  private final ImmutableMap<K, V> map;

  private EnumDelegatingImmutableCompleteMap(Class<K> enumType, Map<K, V> values) {
    checkArgument(enumType.getEnumConstants().length == values.size());
    this.map = Maps.immutableEnumMap(values);
    // this.map = values.isEmpty() ? new EnumMap<>(enumType) : new EnumMap<>(values);
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @SuppressWarnings("unlikely-arg-type")
  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(checkNotNull(key));
  }

  @SuppressWarnings("unlikely-arg-type")
  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(checkNotNull(value));
  }

  @Override
  public ImmutableSet<K> keySet() {
    return map.keySet();
  }

  @Override
  public ImmutableCollection<V> values() {
    return map.values();
  }

  @Override
  public int size() {
    return map.size();
  }

  @SuppressWarnings("unlikely-arg-type")
  @Override
  public V get(Object key) {
    return map.get(checkNotNull(key));
  }

  @Override
  public ImmutableSet<Entry<K, V>> entrySet() {
    return map.entrySet();
  }

  @Deprecated
  @Override
  public V put(K key, V value) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  @Override
  public V remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
}
