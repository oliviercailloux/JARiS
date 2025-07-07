package io.github.oliviercailloux.jaris.io;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import io.github.oliviercailloux.jaris.exceptions.Unchecker;
import io.github.oliviercailloux.jaris.throwing.TPredicate;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A few helper methods generally useful when dealing with {@link Path} instances which are felt to
 * miss from the JDK and Guava.
 */
public class PathUtils {
  @SuppressWarnings("serial")
  private static class InternalException extends RuntimeException {
    public InternalException(IOException e) {
      super(e);
    }

    @Override
    public synchronized IOException getCause() {
      return (IOException) super.getCause();
    }
  }

  /**
   * Wraps any checked exceptions into an InternalException with the checked exception as its cause.
   */
  private static final Unchecker<IOException, InternalException> UNCHECKER =
      Unchecker.wrappingWith(InternalException::new);

  /**
   * Returns every path that is a child of the given {@code start} path, as if invoking
   * {@code Files.find(start, Integer.MAX_VALUE)}, using the given filter to keep only the desired
   * elements.
   *
   * @param start the path to start the search from
   * @param filter the predicate that permits a child in the resulting list only if it returns
   *        {@code true}
   * @param options options to configure the traversal
   * @return the children passing the predicate test, including the starting path if it passes the
   *         predicate test
   * @throws IOException if an I/O error is thrown when accessing a file or if the predicate throws
   */
  public static ImmutableSet<Path> getMatchingChildren(Path start,
      TPredicate<Path, IOException> filter, FileVisitOption... options) throws IOException {
    final Predicate<Path> wrapped = UNCHECKER.wrapPredicate(filter);
    try (Stream<Path> found =
        Files.find(start, Integer.MAX_VALUE, (p, a) -> wrapped.test(p), options)) {
      try {
        return found.collect(ImmutableSet.toImmutableSet());
      } catch (UncheckedIOException e) {
        throw e.getCause();
      }
    } catch (InternalException e) {
      throw e.getCause();
    }
  }

  /**
   * Copies recursively the given source to the given target.
   * 
   * If the paths do not live in the same file system, each name element in the given source path
   * must be a valid single name element in the given target file system. If the source and target
   * live in the same file system, this condition is satisfied, but it may fail otherwise. For
   * example, it fails with the single name element “a\b” in a unix source file system and a windows
   * target file system. See https://github.com/google/jimfs/issues/112.
   * 
   * Thanks to https://stackoverflow.com/a/60621544/.
   *
   * @param source the source to start the copy from
   * @param target the target to copy to, will be created, parent must exist
   * @return the target path
   * @see MoreFiles#deleteRecursively(Path, RecursiveDeleteOption...)
   */
  public static Path copyRecursively(Path source, Path target, CopyOption... options)
      throws IOException {
    Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
          throws IOException {
        Path targetDir = sourceToTarget(dir);
        if (!Files.exists(targetDir)) {
          Files.createDirectory(targetDir);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.copy(file, sourceToTarget(file), options);
        return FileVisitResult.CONTINUE;
      }

      private Path sourceToTarget(Path sourceAbsolutePath) {
        Path sourceRelativePath = source.relativize(sourceAbsolutePath);
        return resolve(target, sourceRelativePath);
      }
    });

    return target;
  }

  /*
   * A generalization of Path#resolve that works even if the paths do not live in the same file
   * system.
   * 
   * If the paths live in the same file system, this method behaves like Path#resolve. If the paths
   * do not live in the same file system, and the given additional path is relative and has no root
   * component, its name elements are added to the given start path.
   * 
   * @param start the path to start the resolution from
   * 
   * @param addition the path to resolve, must be in the same file system as start, or be relative
   * and have no root.
   * 
   * @return a path in the start file system
   * 
   * @throws InvalidPathException if some element of the additional path do not constitute a
   * relative path with a single name element in the file system of the start path (either because
   * the element contains characters that cannot be converted to legal characters, or represents an
   * absolute path, or a path with more than one name element, in the file system of the start
   * path).
   */
  public static Path resolve(Path start, Path addition) throws InvalidPathException {
    if (addition.getFileSystem().equals(start.getFileSystem())) {
      return start.resolve(addition);
    }
    checkArgument(!addition.isAbsolute());
    checkArgument(addition.getRoot() == null);

    Path targetRelativePath = start;
    for (Path element : addition) {
      Path targetElement = start.getFileSystem().getPath(element.toString());
      checkArgument(!targetElement.isAbsolute());
      checkArgument(targetElement.getNameCount() == 1);
      targetRelativePath = targetRelativePath.resolve(targetElement);
    }
    return targetRelativePath;
  }

  /**
   * Rather use
   * {@link MoreFiles#deleteRecursively(Path, com.google.common.io.RecursiveDeleteOption...)}
   */
  static Path deleteRecursively(Path startingPath) throws IOException {
    return Files.walkFileTree(startingPath, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
        if (e != null) {
          throw e;
        }
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
