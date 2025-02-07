package io.github.oliviercailloux.jaris.io;

import java.io.IOException;
import java.nio.file.Path;

class WrapCloseablePath extends ForwardingPath implements CloseablePath {

  private final Path delegate;

  WrapCloseablePath(Path delegate) {
    this.delegate = delegate;
  }

  @Override
  public Path delegate() {
    return delegate;
  }

  @Override
  public void close() throws IOException {
  }
  
}
