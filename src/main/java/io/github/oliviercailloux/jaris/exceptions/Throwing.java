/*
 * Copyright 2016 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.oliviercailloux.jaris.exceptions;

/**
 * <p>
 * Variations on the standard functional interfaces which throw a specific
 * subclass of Throwable.
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
		void run() throws E;
	}

	@FunctionalInterface
	public interface Supplier<T, E extends Throwable> {
		T get() throws E;
	}

	@FunctionalInterface
	public interface Consumer<T, E extends Throwable> {
		void accept(T t) throws E;
	}

	@FunctionalInterface
	public interface Function<T, R, E extends Throwable> {
		R apply(T t) throws E;
	}

	@FunctionalInterface
	public interface Predicate<T, E extends Throwable> {
		boolean test(T t) throws E;
	}

	@FunctionalInterface
	public interface BiConsumer<T, U, E extends Throwable> {
		void accept(T t, U u) throws E;
	}

	@FunctionalInterface
	public interface BiFunction<T, U, R, E extends Throwable> {
		R apply(T t, U u) throws E;
	}

	@FunctionalInterface
	public interface BiPredicate<T, U, E extends Throwable> {
		boolean accept(T t, U u) throws E;
	}
}
