package io.github.oliviercailloux.jaris.io;

import java.io.IOException;
import java.nio.file.Path;

/* A closeable path that derives from another closeable path. */
class SecondaryCloseablePath extends ForwardingPath implements CloseablePath {
  private final Path delegate;
  private final CloseablePath fileSystem;

  SecondaryCloseablePath(CloseablePath toClose, Path delegate) {
    this.fileSystem = toClose;
    this.delegate = delegate;
  }

  @Override
  public Path delegate() {
    return delegate;
  }

  @Override
  public void close() throws IOException {
    fileSystem.close();
  }
  
}
