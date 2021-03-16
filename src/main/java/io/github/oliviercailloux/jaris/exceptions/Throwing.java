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
  public interface Runnable<X extends Throwable> {
    public void run() throws X;
  }

  @FunctionalInterface
  public interface Supplier<T, X extends Throwable> {
    public T get() throws X;
  }

  /**
   * TODO add default methods.
   */
  @FunctionalInterface
  public interface Comparator<T, X extends Throwable> {
    public int compare(T o1, T o2) throws X;
  }

  @FunctionalInterface
  public interface Consumer<T, X extends Throwable> {
    public void accept(T t) throws X;
  }

  @FunctionalInterface
  public interface Function<T, R, X extends Throwable> {
    public R apply(T t) throws X;
  }

  @FunctionalInterface
  public interface Predicate<T, X extends Throwable> {
    public boolean test(T t) throws X;

    public default <E2 extends X> Predicate<T, X> and(Predicate<? super T, E2> p2) {
      return t -> test(t) && p2.test(t);
    }

    public default <E2 extends X> Predicate<T, X> or(Predicate<? super T, E2> p2) {
      return t -> test(t) || p2.test(t);
    }

    public default Predicate<T, X> negate() {
      return t -> !test(t);
    }
  }

  @FunctionalInterface
  public interface BiConsumer<T, U, X extends Throwable> {
    public void accept(T t, U u) throws X;
  }

  @FunctionalInterface
  public interface BiFunction<T, U, R, X extends Throwable> {
    public R apply(T t, U u) throws X;
  }

  @FunctionalInterface
  public interface BiPredicate<T, U, X extends Throwable> {
    public boolean accept(T t, U u) throws X;
  }
}
