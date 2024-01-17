package io.github.oliviercailloux.jaris.exceptions;

import java.util.Random;

public interface MyImpl extends MyInterface<Random, Random> {
  public static MyImpl instance() {
    return new MyImpl() {
      @Override
      public MyImpl andConsume(TConsumer<?> consumer) {
        return this;
      }
    };
  }

  @Override
  public MyImpl andConsume(TConsumer<?> consumer);
}
