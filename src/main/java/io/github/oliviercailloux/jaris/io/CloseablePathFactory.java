package io.github.oliviercailloux.jaris.io;

import io.github.oliviercailloux.jaris.io.CloseablePath;
import io.github.oliviercailloux.jaris.io.PathUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.ProviderNotFoundException;

public interface CloseablePathFactory {
  CloseablePath path() throws ProviderNotFoundException, IOException;

  default CloseablePathFactory resolve(String other) {
    return new CloseablePathFactoryResolving(this, other);
  }
}
