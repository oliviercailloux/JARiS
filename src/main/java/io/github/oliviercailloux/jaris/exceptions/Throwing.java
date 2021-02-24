/*
 * Copyright 2016 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.github.oliviercailloux.jaris.exceptions;

/**
 * <p>
 * Variations on the standard functional interfaces which throw a specific subclass of Throwable.
 * </p>
 * <p>
 * Copied from the <a href=
 * "https://github.com/diffplug/durian/blob/99100976d27a5ebec74a0a7df48fc23de822fa00/src/com/diffplug/common/base/Throwing.java">durian</a>
 * library and simplified.
 * </p>
 */
public interface Throwing {
  @FunctionalInterface
  public interface Runnable<E extends Throwable> {
    public void run() throws E;
  }

  @FunctionalInterface
  public interface Supplier<T, E extends Throwable> {
    public T get() throws E;
  }

  /**
   * TODO add default methods.
   */
  @FunctionalInterface
  public interface Comparator<T, E extends Throwable> {
    public int compare(T o1, T o2) throws E;
  }

  @FunctionalInterface
  public interface Consumer<T, E extends Throwable> {
    public void accept(T t) throws E;
  }

  @FunctionalInterface
  public interface Function<T, R, E extends Throwable> {
    public R apply(T t) throws E;
  }

  @FunctionalInterface
  public interface Predicate<T, E extends Throwable> {
    public boolean test(T t) throws E;

    public default <E2 extends E> Predicate<T, E> and(Predicate<? super T, E2> p2) {
      return t -> test(t) && p2.test(t);
    }

    public default <E2 extends E> Predicate<T, E> or(Predicate<? super T, E2> p2) {
      return t -> test(t) || p2.test(t);
    }

    public default Predicate<T, E> negate() {
      return t -> !test(t);
    }
  }

  @FunctionalInterface
  public interface BiConsumer<T, U, E extends Throwable> {
    public void accept(T t, U u) throws E;
  }

  @FunctionalInterface
  public interface BiFunction<T, U, R, E extends Throwable> {
    public R apply(T t, U u) throws E;
  }

  @FunctionalInterface
  public interface BiPredicate<T, U, E extends Throwable> {
    public boolean accept(T t, U u) throws E;
  }
}
