package io.github.oliviercailloux.jaris.exceptions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * <p>
 * An instance of this class contains either a result (in which case it is called a “success”) or a
 * cause of type {@link Throwable} (in which case it is called a “failure”).
 * </p>
 * <p>
 * Instances of this class are immutable.
 * </p>
 * <p>
 * Heavily inspired by <a href="https://github.com/vavr-io/vavr">Vavr</a>. One notable difference is
 * that this class (and this library) does not sneaky throw. In particular, {@link #get()} does not
 * throw the original cause if this object is a failure (compare with the contract of Vavr’s
 * <code>Try#<a href=
 * "https://github.com/vavr-io/vavr/blob/9a40af5cec2622a8ce068d5833a2bf07671f5eed/src/main/java/io/vavr/control/Try.java#L629">get()</a></code>
 * and its <a href=
 * "https://github.com/vavr-io/vavr/blob/9a40af5cec2622a8ce068d5833a2bf07671f5eed/src/main/java/io/vavr/control/Try.java#L1305">implementation</a>).
 * <p>
 * TODO specific that this library is <code>null</code> hostile
 * <p>
 * <ul>
 * <li>TRun => TryVoid
 * <li>TSupp => Try
 * <li>r TCons => r ; r TCons => c' ; c TCons => c. andCons [if success, merge with a TryVoid; oth.
 * unch.]
 * <li>r TFct => r' ; r TFct => c' ; c TFct => c. andMap [if success, merge with a Try; oth. unch.]
 * <li>r TRun => r ; r TRun => c'; c TRun c. andRun [if success, merge with a TryVoid; oth. unch.]
 * <li>r TSupp => r'; r TSupp => c'; c TSupp c. TryVoid#andGet [if success, merge with a Try; oth.
 * unch.]
 * <li>r TSupp r; c TSupp r'; c TSupp c'. orGet [if failure, merge with a Try; oth. unch.]
 * <li>r TSupp r; c TSupp r'; c TSupp c. orGet+merge exc function keep original exc [if failure,
 * merge with a Try but keep left; oth. unch.]
 * </ul>
 * <p>
 * Terminal operations (no more a try)
 * <ul>
 * <li>r/c (TCons, TCons). consume
 * <li>r TFct r; c TFct r'; c TFct c'. orMapCause
 * <li>TryVoid#consumeCause.
 * </ul>
 * <p>
 * Merge
 * <ul>
 * TODO
 * </ul>
 * <p>
 * TryVoid
 * <ul>
 * <li>andGet => Try
 * <li>andRun => TryVoid
 * <li>orRun => TryVoid [+ merge exc function?]
 * </ul>
 * <p>
 * Optional
 * <ul>
 * <li>filter
 * <li>flatMap
 * <li>ifPresent
 * <li>ifPresentOrElse
 * <li>map
 * <li>or(supp<opt>)
 * <li>orElse(T)
 * <li>orElseGet(supp)
 * <li>orElseThrow
 * <li>stream
 * </ul>
 * <h2>Axiomatics</h2> Let (s, f) designate a try that contains either s or f. Consider the merge
 * operation given two tries: (s, f) and (s', f'). One such operation is defined by four behaviors.
 * <ul>
 * <li>s, s' → F (a user-provided merge function that indicates the result of the merge); s or s'
 * <li>s, f' → s or f'
 * <li>f, s' → f or s'
 * <li>f, f' → F; f or f'.
 * </ul>
 * That’s 3 × 2 × 2 × 3 = 36 possible operations, too much. To reduce it, let’s decide that (R1) s,
 * f' → s ⇔ f, s' → s', thus, in presence of both a success and a failure, either we keep the
 * success (or-based or recovery logic), or we keep the failure (and-based or fail-fast logic),
 * irrespective of their ordering.
 * <p>
 * We are now left with 18 possibilities. We can write each such operation with three letters, e.g.
 * FsF means {s, s' → F; s, f' and f, s' → s; f, f' → F}. We also discuss whether it makes sense
 * when the second try is produced by a function (try-fct) or by a supplier (try-sup) or given. When
 * for a try-sup, there is no point in also providing a version for given.
 * <ul>
 * <li>Fs. Hard to find a use case: we’re considering two tries, but only one of them need to be a
 * success to succeed (so we’re seemingly attempting twice something and discarding a possible
 * failure), but if this succeeds twice, we need to merge the result using a specified merge
 * function. Why would we want to do this, instead of just being happy with any success (say, the
 * first one)? Seems like we don’t care which attempt succeeded or whether one or both succeeded;
 * but we do care to specify how to merge if they both succeed. [Previously I had considered:
 * orMerge(F, F), making sense for given, try-sup.]
 * <li>Fff Better provided by s'ff when failing-fast. and, for given.
 * <li>Fff' can’t fail fast as we may want the second failure. Can’t be a function as has to work
 * also when first failed. No try-sup as has to run anyway. When given, provided by Fff, reversing
 * arguments.
 * <li>ssF or with short-circuit, for try-sup
 * <li>ssf either provided by ssF, alternative: orAttempt for try-sup<Opt>?
 * <li>ssf' the first failure is ignored, so is in fact a merge of an optional with a try. Provide
 * constructor either(Optional, try-sup), or make it possible to transform a try-sup to a
 * Supplier<Try> and use the normal Optional#or.
 * <li>sff Never uses s' thus it’s not really a merge of two tries. andConsume. andRun.
 * <li>sff' Never uses s' thus it’s not really a merge of two tries. can’t fail fast as we may want
 * the second failure. Can’t be a function as has to work also when first failed. No try-sup (or
 * try-run) as has to run anyway. Use tryVoid#andGet with s'ff semantics, reversing arguments.
 * <li>s's. provided by ss. if we don’t care which success we keep, otherwise see Fs.
 * <li>s'ff and with fail-fast, makes sense for try-fct, not for try-sup as not using s ever; rather
 * tryVoid#andGet.
 * <li>s'ff' can’t fail fast as we may want the second failure. Can’t be a function as has to work
 * also when first failed. No try-sup as has to run anyway. Use andRun, with sff semantics,
 * reversing arguments.
 * </ul>
 * <p>
 * Try + TryVoid → Try.
 * <ul>
 * <li>s, ✓ → s
 * <li>s, f → s / f
 * <li>f, ✓ → f
 * <li>f, f' → f / f'
 * </ul>
 * <ul>
 * <li>sf returns this!
 * <li>sf' replaceCauseIfArgumentFailed(TryVoid). Reverse? TryVoid + Try, or? Unneeded: Try#or for
 * try-sup is enough. Unless I first need to consume smth then produce a try then merge them. In
 * which case: prepare.or(try.toVoid).and(try). Or could provide TryVoid#anyway(try), but not clear.
 * <li>ff and(TryVoid)
 * <li>ff' Reverse? TryVoid + Try, and?
 * </ul>
 * <p>
 * TryVoid + Try → Try.
 * <ul>
 * <li>✓, s → s
 * <li>f, s → s / f
 * <li>✓, f → f
 * <li>f, f' → f / f'
 * </ul>
 * <ul>
 * <li>sf see above sf'.
 * <li>sf' returns arg
 * <li>ff andGet for fct-sup; and, for given.
 * <li>ff' reverse arguments, provided by Try#andConsume, and(TryVoid)?
 * </ul>
 * <p>
 * TryVoid.
 * <ul>
 * <li>s'ff andGet; and(Try)
 * <li>and(TryVoid); andRun
 * <li>or; orRun
 * </ul>
 * <p>
 * Summary. Always keep first failure (except for either.)
 * <ul>
 * <li>Fff and(Try, BiFunction)
 * <li>ssF orGet(Supplier, BiFunction); or(Try, BiFunction)
 * <li>[ssf orGet(Supplier); or(Try)]
 * <li>[ssf' either(Opt, Supplier)]
 * <li>sff andIfPresent(Consumer)
 * <li>s'ff flatMap(Function)
 * </ul>
 * <p>
 * TrySafe, no type for the throwable, catch every throwable. Try<T, W extends Exception>, type for
 * the exception, the cause is always a checked exception, catches only this.
 *
 * @param <T> the type of result possibly kept in this object.
 */
public class TrySafe<T> extends TryGeneral<T, Throwable> {
  /**
   * Returns a success containing the given result.
   *
   * @param <T> the type that parameterizes the returned instance
   * @param t the result to contain
   */
  public static <T> TrySafe<T> successSafe(T t) {
    return new TrySafe<>(t, null);
  }

  /**
   * Returns a failure containing the given cause.
   *
   * @param <T> the type that parameterizes the returned instance (mostly irrelevant, as it only
   *        determines which result this instance can hold, but it holds none)
   * @param cause the cause to contain
   */
  public static <T> TrySafe<T> failureSafe(Throwable cause) {
    return new TrySafe<>(null, cause);
  }

  private final T result;
  private final Throwable cause;

  private TrySafe(T t, Throwable cause) {
    final boolean thrown = cause != null;
    final boolean resulted = t != null;
    checkArgument(resulted == !thrown);
    this.cause = cause;
    this.result = t;
  }

  /**
   * Returns <code>true</code> iff this object contains a result (and not a cause).
   *
   * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
   */
  public boolean isSuccess() {
    return result != null;
  }

  /**
   * Return <code>true</code> iff this object contains a cause (and not a result).
   *
   * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
   */
  public boolean isFailure() {
    return cause != null;
  }

  private <T2> TrySafe<T2> castFailure() {
    checkState(isFailure());
    @SuppressWarnings("unchecked")
//    final TrySafe<T2> casted = (TrySafe<T2>) this;
    return casted;
  }

  public <U extends T, X extends Exception> TrySafe<T> orGet(Throwing.Supplier<U, X> supplier) {
    if (isSuccess()) {
      return this;
    }
    final TrySafe<T> t2 = TrySafe.of(supplier);
    if (t2.isSuccess()) {
      return t2;
    }
    return this;
  }

  public <X extends Exception> TrySafe<T> and(Throwing.Consumer<T, X> consumer) {
    if (isFailure()) {
      return this;
    }
    final TryVoidOld t2 = TryVoidOld.run(() -> consumer.accept(result));
    if (t2.isFailure()) {
      return failure(t2.getCause());
    }
    return this;
  }

  /**
   * If this instance is a success, returns a {@link TrySafe} that contains its result transformed
   * by the given transformation or a cause thrown by the given transformation. If this instance is
   * a failure, returns this instance.
   * <p>
   * This method does not throw. If the given function throws while applying it to this instance’s
   * result, the throwable is returned in the resulting try instance.
   *
   * @param <T2> the type of result the transformation produces.
   * @param transformation the function to apply to the result contained in this instance
   * @return a success iff this instance is a success and the transformation function did not throw
   * @see #map(Function)
   */
  public <T2, X extends Exception> TrySafe<T2> flatMap(Throwing.Function<T, T2, X> transformation) {
    final TrySafe<T2> newResult;
    if (isFailure()) {
      newResult = castFailure();
    } else {
      newResult = TrySafe.of(() -> transformation.apply(result));
    }
    return newResult;
  }

  public <T2, X extends Exception, Y extends X, Z extends X> T2 map(
      Throwing.Function<T, T2, Y> transformation,
      Throwing.Function<Throwable, T2, Z> causeTransformation) throws X {
    if (isSuccess()) {
      return transformation.apply(result);
    }
    return causeTransformation.apply(cause);
  }

  /**
   * Returns the result contained in this instance if it is a success; otherwise, returns the result
   * of applying the given transformation to the cause contained in this instance, or throws if the
   * function threw.
   *
   * @param <X> a sort of exception that the given function may throw
   * @param causeTransformation the function to apply to the cause
   * @return the result contained in this instance or the transformed cause
   * @throws X if the given function throws while being applied to the cause contained in this
   *         instance
   */
  public <X extends Exception> T orMapCause(Throwing.Function<Throwable, T, X> causeTransformation)
      throws X {
    checkNotNull(causeTransformation);
    if (isSuccess()) {
      return result;
    }
    return causeTransformation.apply(cause);
  }

  public T orElseThrow() {
    if (isFailure()) {
      throw new NoSuchElementException("This try contains no result");
    }
    return result;
  }

  public <U, R, X extends Exception> TrySafe<R> merge(TrySafe<U> t2,
      Throwing.BiFunction<T, U, R, X> merger) {
    if (isSuccess()) {
      if (t2.isSuccess()) {
        return TrySafe.of(() -> merger.apply(result, t2.result));
      }
      return t2.castFailure();
    }
    return castFailure();
  }

  public Optional<T> toOptional() {
    if (isSuccess()) {
      return Optional.of(result);
    }
    return Optional.empty();
  }

  public TryVoidOld toTryVoid() {
    if (isFailure()) {
      return TryVoidOld.failure(cause);
    }
    return TryVoidOld.success();
  }

  /**
   * Returns <code>true</code> iff the given object is a {@link TrySafe}; is a success or a failure
   * according to whether this instance is a success or a failure; and holds an equal result or
   * cause.
   */
  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof TrySafe)) {
      return false;
    }

    final TrySafe<?> t2 = (TrySafe<?>) o2;
    return Objects.equals(result, t2.result) && Objects.equals(cause, t2.cause);
  }

  @Override
  public int hashCode() {
    return Objects.hash(result, cause);
  }

  /**
   * Returns a string representation of this object, suitable for debug.
   */
  @Override
  public String toString() {
    final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
    if (isSuccess()) {
      stringHelper.add("result", result);
    } else {
      stringHelper.add("cause", cause);
    }
    return stringHelper.toString();
  }

}
