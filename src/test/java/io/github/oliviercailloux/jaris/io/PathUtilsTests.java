package io.github.oliviercailloux.jaris.io;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Jimfs;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathUtilsTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(PathUtilsTests.class);

  @Test
  public void testCopyRecursively() throws Exception {
    try (FileSystem src = Jimfs.newFileSystem()) {
      Path root = Files.createDirectories(src.getPath("root/"));
      Files.createFile(root.resolve("file"));
      Path subdir = Files.createDirectories(root.resolve("subdir/"));
      Files.createFile(subdir.resolve("subfile"));

      Path copied = src.getPath("copied/");
      PathUtils.copyRecursively(root, copied);
      assertTrue(Files.exists(copied.resolve("file")));
      assertTrue(Files.exists(copied.resolve("subdir/subfile")));
    }
  }

  @Test
  public void testCopyRecursivelyDefaultToJim() throws Exception {
    Path root = Path.of("");
    try (FileSystem target = Jimfs.newFileSystem()) {
      Path copied = target.getPath("copied/");
      PathUtils.copyRecursively(root, copied);
      assertTrue(Files.exists(copied.resolve("pom.xml")));
      assertTrue(Files.exists(
          copied.resolve("src/main/java/io/github/oliviercailloux/jaris/io/PathUtils.java")));
    }
  }

  @Test
  public void testCopyRecursivelyCpToJim() throws Exception {
    String classFile = LoggerFactory.class.getSimpleName() + ".class";
    URI uri = LoggerFactory.class.getResource(classFile).toURI();
    assertThrows(FileSystemNotFoundException.class, () -> Path.of(uri));
    try (FileSystem fs = FileSystems.newFileSystem(uri, ImmutableMap.of())) {
      Path root = Path.of(uri).getParent().getParent();
      LOGGER.info("Root: {}.", root);
      try (FileSystem target = Jimfs.newFileSystem()) {
        Path copied = target.getPath("copied/");
        PathUtils.copyRecursively(root, copied);
        assertTrue(Files.exists(copied.resolve("slf4j/").resolve(classFile)));
        assertTrue(Files.exists(
            copied.resolve("slf4j/").resolve("spi/").resolve("SLF4JServiceProvider.class")));
      }
    }
  }

  @Test
  public void testJimfsDoesNotResolveForeignPaths() throws Exception {
    try (FileSystem fs = Jimfs.newFileSystem()) {
      Path defaultEmpty = Path.of("");
      Path jimRoot = fs.getRootDirectories().iterator().next();
      assertThrows(ProviderMismatchException.class, () -> jimRoot.resolve(defaultEmpty));
    }
  }

  @Test
  public void testJimfsDoesNotResolveFriendlyPaths() throws Exception {
    try (FileSystem fs = Jimfs.newFileSystem()) {
      try (FileSystem fs2 = Jimfs.newFileSystem()) {
        Path otherJimfsEmpty = fs2.getPath("");
        Path jimRoot = fs.getRootDirectories().iterator().next();
        assertThrows(ProviderMismatchException.class, () -> jimRoot.resolve(otherJimfsEmpty));
      }
    }
  }
}
