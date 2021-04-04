package io.github.oliviercailloux.jaris.exceptions;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

public abstract class TryVariableCatch<T, X extends Throwable> extends TryOptional<T, X>
    implements TryVariableCatchInterface<T, X> {

  @Override
  public String toString() {
    final ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
    orConsumeCause(e -> stringHelper.add("cause", e)).ifPresent(r -> stringHelper.add("result", r));
    return stringHelper.toString();
  }
}
