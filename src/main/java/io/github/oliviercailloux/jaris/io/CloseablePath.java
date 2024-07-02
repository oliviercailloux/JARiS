package io.github.oliviercailloux.jaris.io;

import com.google.common.collect.ImmutableMap;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.ProviderNotFoundException;

public class CloseablePath extends ForwardingPath implements Closeable {

  private final Path delegate;
  private final FileSystem fsToClose;

  CloseablePath(URI uri) throws IOException, ProviderNotFoundException {
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
  protected Path delegate() {
    return delegate;
  }

  @Override
  public void close() throws IOException {
    if (fsToClose != null) {
      fsToClose.close();
    }
  }
}
