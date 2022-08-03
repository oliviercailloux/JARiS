package io.github.oliviercailloux.jaris.io;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.MoreFiles;
import io.github.oliviercailloux.jaris.exceptions.Throwing;
import io.github.oliviercailloux.jaris.exceptions.Unchecker;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
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
      Throwing.Predicate<Path, IOException> filter, FileVisitOption... options) throws IOException {
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
