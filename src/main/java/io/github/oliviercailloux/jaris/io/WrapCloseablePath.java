package io.github.oliviercailloux.jaris.io;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.ProviderNotFoundException;

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
