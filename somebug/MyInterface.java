package somebug;

import java.util.Random;

public interface MyInterface<Z extends Random> {

  public MyInterface<Random> andConsume(TConsumer<? extends Z> consumer);
}
