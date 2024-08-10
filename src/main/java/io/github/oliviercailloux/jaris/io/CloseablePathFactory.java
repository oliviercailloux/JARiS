package io.github.oliviercailloux.jaris.io;

import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.ProviderNotFoundException;

public interface CloseablePathFactory {
  CloseablePath path() throws ProviderNotFoundException, IOException;

  default CloseablePathFactory resolve(String other) {
    return new CloseablePathFactoryResolving(this, other);
  }

  default ByteSource asByteSource() {
    return new ByteSource() {
      @Override
      public InputStream openStream() throws IOException {
        return Files.newInputStream(path());
      }
    };
  }
}
