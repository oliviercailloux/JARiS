package somebug;

import java.util.Random;

public interface TConsumer<X extends Random> {
  public void consuming(Integer t);
}
