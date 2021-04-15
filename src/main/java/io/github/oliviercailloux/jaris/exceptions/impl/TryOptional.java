package io.github.oliviercailloux.jaris.exceptions.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import io.github.oliviercailloux.jaris.exceptions.Throwing;
import io.github.oliviercailloux.jaris.exceptions.Throwing.BiFunction;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Consumer;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Runnable;
import io.github.oliviercailloux.jaris.exceptions.Throwing.Supplier;
import io.github.oliviercailloux.jaris.exceptions.Try;
import io.github.oliviercailloux.jaris.exceptions.TryVoid;
import io.github.oliviercailloux.jaris.exceptions.catch_all.TryCatchAll;
import io.github.oliviercailloux.jaris.exceptions.catch_all.TryCatchAllVoid;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * The root of the {@code Try*} implementation hierarchy, defining in the most general way the
 * concepts of success, failure, catching, and equality.
 * <p>
 * This is not a public part of the contract of {@code Try*} because {@link #catchesAll()}, for
 * example, should not be exposed: different catching behaviors are publicly viewed as unrelated
 * types.
 * <p>
 * Is homeomorphic to an {@code Optional<T>} xor {@code X} (plus indication of catching checked or
 * catching all): either is a success, and then <em>may</em> contain a result of type {@code T}, or
 * is a failure, and then <em>does</em> contain a cause of type {@code X}.
 * <p>
 * <h2>Summary</h2>
 * <p>
 * Try + Try
 * <ul>
 * <li>Fs. See ssF and s'sF.
 * <li>Fff Better provided by s'ff when failing-fast. and, for given.
 * <li>Fff' can’t fail fast as we may want the second failure. Can’t be a function as has to work
 * also when first failed. No try-sup as has to run anyway. When given, provided by Fff, reversing
 * arguments.
 * <li>ssF or with short-circuit, for try-sup
 * <li>sff Not a merge of two tries (never uses s'). For given: Fff. for try-sup: andRun for
 * try-fct: andConsume
 * <li>sff' Never uses s' thus it’s not really a merge of two tries. can’t fail fast as we may want
 * the second failure. Can’t be a function as has to work also when first failed. No try-sup (or
 * try-run) as has to run anyway. Use and, reversing arguments. [Use tryVoid#andGet with s'ff
 * semantics, reversing arguments.]
 * <li>s'sF provided by ssF, reversing arguments.
 * <li>s'ff and with fail-fast, flatMap for try-fct, not for try-sup as not using s ever (use
 * flatMap and do nothing with the input). For given: Fff.
 * <li>s'ff' can’t fail fast as we may want the second failure. Can’t be a function as has to work
 * also when first failed. No try-sup as has to run anyway. Use andRun, with sff semantics,
 * reversing arguments.
 * </ul>
 * Could also cover the case where we want to act differently depending on success + failure or
 * failure + success, with: ifSameStatusMerge(Try t2, BiFct<T, T → U>, BiFct<X, X → Y>). If
 * different status, keep this. Alternatively: ifTwoFailuresUse(Try t2); ifTwoSuccessesGet(Try t2).
 * (Then can chain ifTwoSuccessesGet(t2).ifTwoFailuresGet(t2) to do both.)
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
 * <li>sf' Corresponds to ≠ actions for (success and failure) than for (failure and success), thus,
 * not covered.
 * <li>ff andRun, andConsume
 * <li>ff' Reverse arguments: TryVoid + Try, andGet
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
 * <li>ff andGet for fct-sup; andRun.
 * <li>ff' reverse arguments, provided by Try#andConsume, #andRun.
 * </ul>
 * <h2>Draft</h2>
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
 * TO DO
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
 * @param <X> the type of cause kept in this object if it is a failure.
 */
public abstract class TryOptional<T, X extends Throwable> {

  /**
   * A sort of try optional such that a success has an associated value. Is homeomorphic to a
   * {@code T} xor {@code X} (plus indication of catching checked or catching all). Suitable for
   * {@link Try} and {@link TryCatchAll}, depending on the catching strategy. The name (“variable
   * catch”) indicates that this interface applies to both catching strategies.
   *
   * @param <T> the type of result kept in this object if it is a success.
   * @param <X> the type of cause kept in this object if it is a failure.
   * @param <Z> a priori constraint applied to some functionals on the type of throwable that they
   *        may throw – when catching all, it sometimes makes sense to authorize functionals to
   *        throw {@code Throwable}; when catching checked, this makes no sense and we reduce
   *        possible signatures to clarify the intended use.
   */
  public interface TryVariableCatchInterface<T, X extends Z, Z extends Throwable> {

    /**
     * Returns <code>true</code> iff this instance contains a result (and not a cause).
     *
     * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
     */
    public boolean isSuccess();

    /**
     * Return <code>true</code> iff this object contains a cause (and not a result).
     *
     * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
     */
    public boolean isFailure();

    /**
     * Returns the transformed result contained in this instance if it is a success, using the
     * provided {@code transformation}; or the transformed cause contained in this instance if it is
     * a failure, using the provided {@code causeTransformation}.
     * <p>
     * This method necessarily applies exactly one of the provided functions.
     *
     * @param <U> the type of transformed result to return
     * @param <Y> a type of exception that the provided functions may throw
     * @param transformation a function to apply to the result if this instance is a success
     * @param causeTransformation a function to apply to the cause if this instance is a failure
     * @return the transformed result or cause
     * @throws Y iff the function that was applied threw an exception of type {@code Y}
     */
    public <U, Y extends Exception> U map(
        Throwing.Function<? super T, ? extends U, ? extends Y> transformation,
        Throwing.Function<? super X, ? extends U, ? extends Y> causeTransformation) throws Y;

    /**
     * Returns the result contained in this instance if it is a success, without applying the
     * provided function; or returns the transformed cause contained in this instance if it is a
     * failure, using the provided {@code causeTransformation}.
     * <p>
     * Equivalent to: {@code map(t -> t, causeTransformation)}.
     *
     * @param <Y> a type of exception that the provided function may throw
     * @param causeTransformation the function to apply iff this instance is a failure
     * @return the result, or the transformed cause
     * @throws Y iff the function was applied and threw an exception of type {@code Y}
     */
    public <Y extends Exception> T orMapCause(
        Throwing.Function<? super X, ? extends T, Y> causeTransformation) throws Y;

    /**
     * Returns an optional containing the result of this instance, without invoking the given
     * consumer, if this try is a success; otherwise, invokes the given consumer and returns an
     * empty optional.
     *
     * @param <Y> a type of exception that the provided consumer may throw
     * @param consumer the consumer to invoke if this instance is a failure
     * @return an optional, containing the result if this instance is a success, empty otherwise
     * @throws Y iff the consumer was invoked and threw an exception of type {@code Y}
     */
    public <Y extends Exception> Optional<T> orConsumeCause(
        Throwing.Consumer<? super X, Y> consumer) throws Y;

    /**
     * Returns the result contained in this instance if this instance is a success, or throws the
     * cause contained in this instance.
     * <p>
     * Equivalent to: {@link #orThrow(Function) orThrow(t -> t)}.
     *
     * @return the result that this success contains
     * @throws X iff this instance is a failure
     */
    public T orThrow() throws X;

    /**
     * Returns the result contained in this instance if this instance is a success, or throws the
     * transformed cause contained in this instance.
     *
     * @param <Y> the type of throwable to throw if this instance is a failure
     * @param causeTransformation the function to apply to the cause iff this instance is a failure
     * @return the result that this success contains
     * @throws Y iff this instance is a failure
     */
    public <Y extends Z> T orThrow(Function<X, Y> causeTransformation) throws Y;

    /**
     * Runs the runnable iff this instance is a success, and returns this instance if it succeeds
     * and the cause of failure if it throws a catchable throwable; otherwise, returns this
     * instance.
     *
     * @param runnable the runnable to invoke iff this instance is a success
     * @return a success iff this instance is a success and the provided runnable does not throw
     */
    public TryVariableCatchInterface<T, X, Z> andRun(Throwing.Runnable<? extends X> runnable);

    /**
     * Runs the consumer iff this instance is a success, and returns this instance if it succeeds
     * and the cause of failure if it throws a catchable throwable; otherwise, returns this
     * instance.
     *
     * @param consumer the consumer to invoke iff this instance is a success
     * @return a success iff this instance is a success and the provided consumer does not throw
     */
    public TryVariableCatchInterface<T, ?, Z> andConsume(
        Throwing.Consumer<? super T, ? extends X> consumer);

    /**
     * Applies the given mapper iff this instance is a success, and returns the transformed success
     * if it succeeds or the cause of the failure if it throws a catchable throwable; otherwise,
     * returns this instance.
     * <p>
     * Equivalent to: {@code t.map(s -> Try.get(() -> mapper.apply(s)), t)}
     *
     * @param <U> the type of result that the returned try will be declared to contain
     * @param mapper the mapper to apply to the result iff this instance is a success
     * @return a success iff this instance is a success and the provided mapper does not throw
     */
    public <U> TryVariableCatchInterface<U, X, Z> andApply(
        Throwing.Function<? super T, ? extends U, ? extends X> mapper);

    /**
     * Returns a string representation of this object, suitable for debug.
     */
    @Override
    public abstract String toString();

  }
  /**
   * A sort of try optional such that a success has no associated value. Is homeomorphic to an
   * {@code Optional<X>} (plus indication of catching checked or catching all). Suitable for
   * {@link TryVoid} and {@link TryCatchAllVoid}, depending on the catching strategy. The name
   * (“variable catch”) indicates that this interface applies to both catching strategies.
   *
   * @param <X> the type of cause kept in this object if it is a failure.
   * @param <Z> a priori constraint applied to some functionals on the type of throwable that they
   *        may throw – when catching all, it sometimes makes sense to authorize functionals to
   *        throw {@code Throwable}; when catching checked, this makes no sense and we reduce
   *        possible signatures to clarify the intended use.
   */
  public interface TryVoidVariableCatchInterface<X extends Z, Z extends Throwable> {

    /**
     * Returns <code>true</code> iff this instance is a success, hence, contains no cause.
     *
     * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
     */
    public boolean isSuccess();



    /**
     * Return <code>true</code> iff this instance contains a cause.
     *
     * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
     */
    public boolean isFailure();



    /**
     * Returns the supplied result if this instance is a success, using the provided
     * {@code supplier}; or the transformed cause contained in this instance if it is a failure,
     * using the provided {@code causeTransformation}.
     * <p>
     * This method necessarily invokes exactly one of the provided functional interfaces.
     *
     * @param <T> the type of (supplied or transformed) result to return
     * @param <Y> a type of exception that the provided functions may throw
     * @param supplier a supplier to get a result from if this instance is a success
     * @param causeTransformation a function to apply to the cause if this instance is a failure
     * @return the supplied result or transformed cause
     * @throws Y iff the functional interface that was invoked threw an exception of type {@code Y}
     */
    public <T, Y extends Exception> T map(Throwing.Supplier<? extends T, ? extends Y> supplier,
        Throwing.Function<? super X, ? extends T, ? extends Y> causeTransformation) throws Y;

    /**
     * If this instance is a failure, invokes the given consumer using the cause contained in this
     * instance. If this instance is a success, do nothing.
     *
     * @param <Y> a type of exception that the provided consumer may throw
     * @param consumer the consumer to invoke if this instance is a failure
     * @throws Y iff the consumer was invoked and threw an exception of type {@code Y}
     */
    public <Y extends Exception> void ifFailed(Throwing.Consumer<? super X, Y> consumer) throws Y;

    /**
     * If this instance is a failure, throws the cause it contains. Otherwise, do nothing.
     * <p>
     * Equivalent to: {@link #orThrow(Function) orThrow(t -> t)}.
     *
     * @throws X iff this instance contains a cause
     */
    public void orThrow() throws X;

    /**
     * If this instance is a failure, throws the transformed cause it contains. Otherwise, do
     * nothing.
     *
     * @param <Y> the type of throwable to throw if this instance is a failure
     * @param causeTransformation the function to apply to the cause iff this instance is a failure
     * @return the result that this success contains
     * @throws Y iff this instance is a failure
     */
    public <Y extends Z> void orThrow(Function<X, Y> causeTransformation) throws Y;

    /**
     * If this instance is a success, returns a try representing the result of invoking the given
     * supplier; otherwise, returns this failure.
     * <p>
     * If this instance is a failure, it is returned, without invoking the given supplier.
     * Otherwise, the given supplier is invoked. If it terminates without throwing, a success is
     * returned, containing the just supplied result. If the supplier throws a catchable throwable,
     * a failure is returned, containing the cause it threw.
     *
     * @param <T> the type of result that the returned try will be declared to contain
     * @param <X> the type of cause that the returned try will be declared to contain
     * @param supplier the supplier to attempt to get a result from if this instance is a success.
     * @return a success iff this instance is a success and the given supplier terminated without
     *         throwing.
     */
    public <T> TryVariableCatchInterface<T, X, Z> andGet(
        Throwing.Supplier<? extends T, ? extends X> supplier);

    /**
     * If this instance is a success, returns a try void representing the result of invoking the
     * given runnable; otherwise, returns this failure.
     * <p>
     * If this instance is a failure, it is returned, without invoking the given runnable.
     * Otherwise, the given runnable is invoked. If it terminates without throwing, a success is
     * returned. If the runnable throws a catchable throwable, a failure is returned, containing the
     * cause it threw.
     *
     * @param <X> the type of cause that the returned instance will be declared to contain
     * @param runnable the runnable to attempt to run if this instance is a success.
     * @return a success iff this instance is a success and the given runnable terminated without
     *         throwing.
     */
    public TryVoidVariableCatchInterface<X, Z> andRun(Throwing.Runnable<? extends X> runnable);

    /**
     * Returns this instance if it is a success; otherwise, returns a try void representing the
     * result of invoking the given runnable.
     * <p>
     * If this instance is a success, it is returned, without invoking the given runnable.
     * Otherwise, the given runnable is invoked. If it terminates without throwing, a success is
     * returned. If the runnable throws a catchable throwable, a failure is returned, containing the
     * cause it threw.
     *
     * @param <X> the type of cause that the returned instance will be declared to contain
     * @param runnable the runnable to attempt to invoke if this instance is a failure.
     * @return a success iff this instance is a success or the given runnable terminated without
     *         throwing.
     */
    public TryVoidVariableCatchInterface<X, Z> or(Throwing.Runnable<? extends X> runnable);



    /**
     * Returns a string representation of this object, suitable for debug.
     */
    @Override
    public abstract String toString();

  }

  public static abstract class TryVariableCatch<T, X extends Z, Z extends Throwable>
      extends TryOptional<T, X> implements TryVariableCatchInterface<T, X, Z> {
    @Override
    public <Y extends Exception> T orMapCause(
        Throwing.Function<? super X, ? extends T, Y> causeTransformation) throws Y {
      return map(t -> t, causeTransformation);
    }

    @Override
    public T orThrow() throws X {
      return orThrow(t -> t);
    }

    @Override
    public String toString() {
      final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
      orConsumeCause(e -> stringHelper.add("cause", e))
          .ifPresent(r -> stringHelper.add("result", r));
      return stringHelper.toString();
    }
  }

  public static abstract class TryVariableCatchSuccess<T, X extends Z, Z extends Throwable>
      extends TryVariableCatch<T, X, Z> {

    protected final T result;

    protected TryVariableCatchSuccess(T result) {
      this.result = checkNotNull(result);
    }

    @Override
    Optional<T> getResult() {
      return Optional.of(result);
    }

    @Override
    Optional<X> getCause() {
      return Optional.empty();
    }

    @Override
    public <U, Y extends Exception> U map(
        Throwing.Function<? super T, ? extends U, ? extends Y> transformation,
        Throwing.Function<? super X, ? extends U, ? extends Y> causeTransformation) throws Y {
      return transformation.apply(result);
    }

    @Override
    public <Y extends Exception> Optional<T> orConsumeCause(
        Throwing.Consumer<? super X, Y> consumer) throws Y {
      return Optional.of(result);
    }

    @Override
    public <Y extends Z> T orThrow(Function<X, Y> causeTransformation) {
      return result;
    }
  }

  public static abstract class TryVariableCatchFailure<X extends Z, Z extends Throwable>
      extends TryVariableCatch<Object, X, Z> {

    protected final X cause;

    protected TryVariableCatchFailure(X cause) {
      this.cause = checkNotNull(cause);
    }

    @Override
    Optional<Object> getResult() {
      return Optional.empty();
    }

    @Override
    Optional<X> getCause() {
      return Optional.of(cause);
    }

    @Override
    public <U, Y extends Exception> U map(
        Throwing.Function<? super Object, ? extends U, ? extends Y> transformation,
        Throwing.Function<? super X, ? extends U, ? extends Y> causeTransformation) throws Y {
      return causeTransformation.apply(cause);
    }

    @Override
    public <Y extends Exception> Optional<Object> orConsumeCause(
        Throwing.Consumer<? super X, Y> consumer) throws Y {
      consumer.accept(cause);
      return Optional.empty();
    }

    @Override
    public <Y extends Z> Object orThrow(Function<X, Y> causeTransformation) throws Y {
      throw causeTransformation.apply(cause);
    }
  }

  public static abstract class TryVoidVariableCatch<X extends Z, Z extends Throwable>
      extends TryOptional<Object, X> implements TryVoidVariableCatchInterface<X, Z> {
    @Override
    public void orThrow() throws X {
      orThrow(t -> t);
    }


    @Override
    public String toString() {
      final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
      andRun(() -> stringHelper.addValue("success"));
      ifFailed(e -> stringHelper.add("cause", e));
      return stringHelper.toString();
    }
  }

  /**
   * TODO
   *
   * @param <X>
   * @param <Z>
   */
  public static abstract class TryVoidVariableCatchSuccess<X extends Z, Z extends Throwable>
      extends TryVoidVariableCatch<X, Z> {

    protected TryVoidVariableCatchSuccess() {
      /* Reducing visibility. */
    }

    @Override
    Optional<Object> getResult() {
      return Optional.empty();
    }

    @Override
    Optional<X> getCause() {
      return Optional.empty();
    }

    @Override
    public <T, Y extends Exception> T map(Throwing.Supplier<? extends T, ? extends Y> supplier,
        Throwing.Function<? super X, ? extends T, ? extends Y> causeTransformation) throws Y {
      return supplier.get();
    }

    @Override
    public <Y extends Exception> void ifFailed(Throwing.Consumer<? super X, Y> consumer) throws Y {
      /* Nothing to do. */
    }

    @Override
    public <Y extends Z> void orThrow(Function<X, Y> causeTransformation) {
      /* Nothing to do. */
    }
  }

  public static abstract class TryVoidVariableCatchFailure<X extends Z, Z extends Throwable>
      extends TryVoidVariableCatch<X, Z> {

    protected final X cause;

    protected TryVoidVariableCatchFailure(X cause) {
      this.cause = checkNotNull(cause);
    }

    @Override
    Optional<Object> getResult() {
      return Optional.empty();
    }

    @Override
    Optional<X> getCause() {
      return Optional.of(cause);
    }

    @Override
    public <T, Y extends Exception> T map(Throwing.Supplier<? extends T, ? extends Y> supplier,
        Throwing.Function<? super X, ? extends T, ? extends Y> causeTransformation) throws Y {
      return causeTransformation.apply(cause);
    }

    @Override
    public <Y extends Exception> void ifFailed(Throwing.Consumer<? super X, Y> consumer) throws Y {
      consumer.accept(cause);
    }

    @Override
    public <Y extends Z> void orThrow(Function<X, Y> causeTransformation) throws Y {
      throw causeTransformation.apply(cause);
    }
  }

  public static class TrySuccess<T> extends TryVariableCatchSuccess<T, Exception, Exception>
      implements Try<T, Exception> {
    public static <T, X extends Exception> Try<T, X> given(T result) {
      return new TrySuccess<>(result).cast();
    }

    private TrySuccess(T result) {
      super(result);
    }

    @Override
    protected boolean catchesAll() {
      return false;
    }

    private <Y extends Exception> Try<T, Y> cast() {
      /*
       * Safe: there is no cause in this (immutable) instance, thus its declared type does not
       * matter.
       */
      @SuppressWarnings("unchecked")
      final Try<T, Y> casted = (Try<T, Y>) this;
      return casted;
    }

    @Override
    public Try<T, Exception> andRun(Runnable<? extends Exception> runnable) {
      final TryVoid<? extends Exception> ran = TryVoid.run(runnable);
      return ran.map(() -> this, Try::failure);
    }

    @Override
    public Try<T, Exception> andConsume(Consumer<? super T, ? extends Exception> consumer) {
      return andRun(() -> consumer.accept(result));
    }

    @Override
    public <U, V, Y extends Exception> Try<V, Exception> and(Try<U, ? extends Exception> t2,
        BiFunction<? super T, ? super U, ? extends V, Y> merger) throws Y {
      return t2.map(u -> Try.success(merger.apply(result, u)), Try::failure);
    }

    @Override
    public <U> Try<U, Exception> andApply(
        Throwing.Function<? super T, ? extends U, ? extends Exception> mapper) {
      return Try.get(() -> mapper.apply(result));
    }

    @Override
    public <Y extends Exception, Z extends Exception, W extends Exception> Try<T, Z> or(
        Supplier<? extends T, Y> supplier,
        BiFunction<? super Exception, ? super Y, ? extends Z, W> exceptionsMerger) throws W {
      return cast();
    }
  }
  public static class TryFailure<X extends Exception>
      extends TryOptional.TryVariableCatchFailure<X, Exception> implements Try<Object, X> {
    public static <T, X extends Exception> Try<T, X> given(X cause) {
      return new TryFailure<>(cause).cast();
    }

    private TryFailure(X cause) {
      super(cause);
    }

    @Override
    protected boolean catchesAll() {
      return false;
    }

    private <U> Try<U, X> cast() {
      /*
       * Safe: there is no result in this (immutable) instance, thus its declared type does not
       * matter.
       */
      @SuppressWarnings("unchecked")
      final Try<U, X> casted = (Try<U, X>) this;
      return casted;
    }

    @Override
    public Try<Object, X> andRun(Throwing.Runnable<? extends X> runnable) {
      return this;
    }

    @Override
    public Try<Object, X> andConsume(Throwing.Consumer<? super Object, ? extends X> consumer) {
      return this;
    }

    @Override
    public <U, V, Y extends Exception> Try<V, X> and(Try<U, ? extends X> t2,
        BiFunction<? super Object, ? super U, ? extends V, Y> merger) throws Y {
      return cast();
    }

    @Override
    public <U> Try<U, X> andApply(
        Throwing.Function<? super Object, ? extends U, ? extends X> mapper) {
      return cast();
    }

    @Override
    public <Y extends Exception, Z extends Exception, W extends Exception> Try<Object, Z> or(
        Throwing.Supplier<? extends Object, Y> supplier,
        Throwing.BiFunction<? super X, ? super Y, ? extends Z, W> exceptionsMerger) throws W {
      final Try<Object, Y> t2 = Try.get(supplier);
      return t2.map(Try::success, y -> Try.failure(exceptionsMerger.apply(cause, y)));
    }

  }

  public static class TryVoidSuccess extends TryVoidVariableCatchSuccess<Exception, Exception>
      implements TryVoid<Exception> {
    public static <X extends Exception> TryVoid<X> given() {
      return new TryVoidSuccess().cast();
    }

    private TryVoidSuccess() {
      /* Reducing visibility. */
    }

    @Override
    protected boolean catchesAll() {
      return false;
    }

    private <Y extends Exception> TryVoid<Y> cast() {
      /*
       * Safe: there is no cause in this (immutable) instance, thus its declared type does not
       * matter.
       */
      @SuppressWarnings("unchecked")
      final TryVoid<Y> casted = (TryVoid<Y>) this;
      return casted;
    }

    @Override
    public <T> Try<T, Exception> andGet(Supplier<? extends T, ? extends Exception> supplier) {
      return Try.get(supplier);
    }

    @Override
    public TryVoid<Exception> andRun(Runnable<? extends Exception> runnable) {
      return TryVoid.run(runnable);
    }

    @Override
    public TryVoid<Exception> or(Runnable<? extends Exception> runnable) {
      return this;
    }
  }

  public static class TryVoidFailure<X extends Exception>
      extends TryVoidVariableCatchFailure<X, Exception> implements TryVoid<X> {
    public static <X extends Exception> TryVoid<X> given(X cause) {
      return new TryVoidFailure<>(cause);
    }

    private TryVoidFailure(X cause) {
      super(cause);
    }

    @Override
    protected boolean catchesAll() {
      return false;
    }

    @Override
    public <T> Try<T, X> andGet(Supplier<? extends T, ? extends X> supplier) {
      return Try.failure(cause);
    }

    @Override
    public TryVoid<X> andRun(Runnable<? extends X> runnable) {
      return this;
    }

    @Override
    public TryVoid<X> or(Runnable<? extends X> runnable) {
      return TryVoid.run(runnable);
    }

  }


  protected TryOptional() {
    /* Reducing visibility. */
  }

  /**
   * Returns <code>true</code> iff this instance represents a success.
   *
   * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
   */
  public boolean isSuccess() {
    return getCause().isEmpty();
  }

  /**
   * Return <code>true</code> iff this instance contains a cause.
   *
   * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
   */
  public boolean isFailure() {
    return getCause().isPresent();
  }

  protected abstract boolean catchesAll();

  abstract Optional<T> getResult();

  abstract Optional<X> getCause();

  /**
   * Returns <code>true</code> iff both instances have the same “catching” behavior, and either:
   * <ul>
   * <li>the given object is a {@link Try} and this object and the given one are both successes and
   * hold equal results;
   * <li>the given object is a {@link Try} or a {@link TryVoid} and this object and the given one
   * are both failures and hold equal causes.
   * </ul>
   */
  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof TryOptional)) {
      return false;
    }

    final TryOptional<?, ?> t2 = (TryOptional<?, ?>) o2;
    return getResult().equals(t2.getResult()) && getCause().equals(t2.getCause())
        && (catchesAll() == t2.catchesAll());
  }

  @Override
  public int hashCode() {
    return Objects.hash(catchesAll(), getResult(), getCause());
  }

  /**
   * Returns a string representation of this object, suitable for debug.
   */
  @Override
  public abstract String toString();
}
