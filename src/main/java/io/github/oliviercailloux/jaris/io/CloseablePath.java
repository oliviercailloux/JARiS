package io.github.oliviercailloux.jaris.io;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A path that delegates to an underlying path (generally, used to instanciate this class) and that
 * can be closed (for example, the underlying file system).
 */
public interface CloseablePath extends Path, AutoCloseable {

  /**
   * Returns the delegate to this path.
   * 
   * This method should generally not be used, as this object can in most cases be used
   * transparently as the original path. This method is useful in case a user of this instance
   * requires access to an interface that the underlying path is expected to implement, or checks
   * the class that it implements.
   *
   * @return the path that this instance delegates to.
   * 
   * @see <a href=
   *      "https://github.com/oliviercailloux/JARiS/blob/main/src/test/java/io/github/oliviercailloux/jaris/io/PathUtilsTests.java#testReadCloseablePaths">PathUtilsTests</a>
   * 
   */
  Path delegate();

  @Override
  void close() throws IOException;
  
}
