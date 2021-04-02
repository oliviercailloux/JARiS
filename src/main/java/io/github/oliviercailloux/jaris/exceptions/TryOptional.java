package io.github.oliviercailloux.jaris.exceptions;

import java.util.Objects;
import java.util.Optional;

/**
 * An internal try type for extension by Try and TryVoid. (And possibly the safe versions as well?)
 * <p>
 * Is homeomorphic to an {@code Optional<T>} xor {@code X}: either is a success, and then
 * <em>may</em> contain a result of type {@code T}, or is a failure, and then <em>does</em> contain
 * a cause of type {@code X}.
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
 * @param <X> the type of cause possibly kept in this object.
 */
abstract class TryOptional<T, X extends Throwable> {
  protected TryOptional() {
    /* Reducing visibility. */
  }

  /**
   * Returns <code>true</code> iff this instance represents a success.
   *
   * @return <code>true</code> iff {@link #isFailure()} returns <code>false</code>
   */
  public abstract boolean isSuccess();

  /**
   * Return <code>true</code> iff this instance contains a cause.
   *
   * @return <code>true</code> iff {@link #isSuccess()} returns <code>false</code>
   */
  public abstract boolean isFailure();

  abstract Optional<T> getResult();

  abstract Optional<X> getCause();

  @Override
  public abstract boolean equals(Object o2);

  @Override
  public int hashCode() {
    return Objects.hash(getResult(), getCause());
  }

  /**
   * Returns a string representation of this object, suitable for debug.
   */
  @Override
  public abstract String toString();

}
