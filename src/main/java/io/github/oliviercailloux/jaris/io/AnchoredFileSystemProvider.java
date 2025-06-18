package io.github.oliviercailloux.jaris.io;

import java.nio.file.FileSystem;
import java.nio.file.Path;

public interface AnchoredFileSystemProvider {
  public FileSystem open();
  /**
   * The anchor path, or throws iff open() has not been called yet.
   * @return a path in the file system opened by open().
   * @throws IllegalStateException if open() has not been called yet.
   */
  public Path path();
}
