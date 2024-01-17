package somebug;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public interface MyInterface<Z extends Random> {

  public MyInterface<Random> andConsume(TConsumer<? extends Z> consumer);
}
