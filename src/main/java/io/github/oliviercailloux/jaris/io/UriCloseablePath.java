package io.github.oliviercailloux.jaris.io;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.ProviderNotFoundException;

class UriCloseablePath extends ForwardingPath implements CloseablePath {

  private final Path delegate;
  private final FileSystem fsToClose;

  UriCloseablePath(URI uri) throws IOException, ProviderNotFoundException {
    Path path;
    FileSystem fs;
    try {
      path = Path.of(uri);
      fs = null;
    } catch (@SuppressWarnings("unused") FileSystemNotFoundException e) {
      fs = FileSystems.newFileSystem(uri, ImmutableMap.of());
      path = Path.of(uri);
    }
    delegate = path;
    fsToClose = fs;
  }

  @Override
  public Path delegate() {
    return delegate;
  }

  @Override
  public void close() throws IOException {
    if (fsToClose != null) {
      fsToClose.close();
    }
  }
}
