package io.github.oliviercailloux.jaris.io;

import java.io.IOException;
import java.nio.file.ProviderNotFoundException;

class CloseablePathFactoryResolving implements CloseablePathFactory {
  private final CloseablePathFactory factory;
  private final String other;

  CloseablePathFactoryResolving(CloseablePathFactory factory, String other) {
    this.factory = factory;
    this.other = other;
  }

  @Override
  public CloseablePath path() throws ProviderNotFoundException, IOException {
    /* The secondary one will take care of closing this path. */
    CloseablePath path = factory.path();
    return new SecondaryCloseablePath(path, path.resolve(path.getFileSystem().getPath(other)));
  }
}
