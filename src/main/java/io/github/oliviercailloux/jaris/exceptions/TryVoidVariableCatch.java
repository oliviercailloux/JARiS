package io.github.oliviercailloux.jaris.exceptions;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

public abstract class TryVoidVariableCatch<X extends Throwable, Z extends Throwable>
    extends TryOptional<Object, X> implements TryVoidVariableCatchInterface<X, Z> {

  @Override
  public String toString() {
    final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
    andRun(() -> stringHelper.addValue("success"));
    ifFailed(e -> stringHelper.add("cause", e));
    return stringHelper.toString();
  }
}
