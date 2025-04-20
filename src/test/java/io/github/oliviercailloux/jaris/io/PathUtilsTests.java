package io.github.oliviercailloux.jaris.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MoreCollectors;
import com.google.common.io.MoreFiles;
import com.google.common.jimfs.Jimfs;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import org.junit.jupiter.api.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathUtilsTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(PathUtilsTests.class);

  @Test
  public void testReadCloseablePaths() throws Exception {
    String classFile = LoggerFactory.class.getSimpleName() + ".class";
    URI uri = LoggerFactory.class.getResource(classFile).toURI();
    assertThrows(FileSystemNotFoundException.class, () -> Path.of(uri));
    try (CloseablePath path = PathUtils.fromUri(uri).path()) {
      assertThrows(ProviderMismatchException.class, () -> Files.newByteChannel(path));
      try (SeekableByteChannel byteChannel = Files.newByteChannel(path.delegate())) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        int nb = byteChannel.read(buffer);
        assertEquals(4, nb);
        int firstInt = buffer.getInt(0);
        assertEquals("cafebabe", Integer.toHexString(firstInt));
      }
    }
  }

  @Test
  public void testReadResolvedCloseablePaths() throws Exception {
    String classFile = LoggerFactory.class.getSimpleName() + ".class";
    URI pckgUri = LoggerFactory.class.getResource("").toURI();
    assertThrows(FileSystemNotFoundException.class, () -> Path.of(pckgUri));
    try (CloseablePath path = PathUtils.fromUri(pckgUri).resolve(classFile).path()) {
      assertThrows(ProviderMismatchException.class, () -> Files.newByteChannel(path));
      try (SeekableByteChannel byteChannel = Files.newByteChannel(path.delegate())) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        int nb = byteChannel.read(buffer);
        assertEquals(4, nb);
        int firstInt = buffer.getInt(0);
        assertEquals("cafebabe", Integer.toHexString(firstInt));
      }
    }
  }

  @Test
  public void testNestedCloseablePaths() throws Exception {
    String classFile = LoggerFactory.class.getSimpleName() + ".class";
    URI uri = LoggerFactory.class.getResource(classFile).toURI();
    LOGGER.info("URI: {}.", uri);
    URI uri2 =
        ILoggerFactory.class.getResource(ILoggerFactory.class.getSimpleName() + ".class").toURI();
    assertThrows(FileSystemNotFoundException.class, () -> Path.of(uri));
    try (CloseablePath p1 = PathUtils.fromUri(uri).path()) {
      try (CloseablePath p2 = PathUtils.fromUri(uri2).path()) {
        assertEquals("cafebabe", Integer.toHexString(
            ByteBuffer.wrap(MoreFiles.asByteSource(p2.delegate()).slice(0, 4).read()).getInt()));
      }
      assertEquals("cafebabe", Integer.toHexString(
          ByteBuffer.wrap(MoreFiles.asByteSource(p1.delegate()).slice(0, 4).read()).getInt()));
    }
  }

  @Test
  public void testListClasspath() throws Exception {
    URL cat = getClass().getResource("/io/github/oliviercailloux/docbook/catalog.xml");
    assertNotNull(cat);
    URI uri = cat.toURI();

    CloseablePathFactory dot =
        PathUtils.fromResource(getClass(), "/io/github/oliviercailloux/docbook/");
    try (CloseablePath dotPath = dot.path()) {
      assertNotNull(dotPath);
      ImmutableSet<Path> content =
          Files.list(dotPath.delegate()).collect(ImmutableSet.toImmutableSet());
      ImmutableSet<URI> uris =
          content.stream().map(Path::toUri).collect(ImmutableSet.toImmutableSet());
      assertTrue(uris.stream().anyMatch(u -> u.toString().endsWith("catalog.xml")));
      URI foundCatalog = uris.stream().filter(u -> u.toString().endsWith("catalog.xml"))
          .collect(MoreCollectors.onlyElement());
      assertEquals(foundCatalog.toString().replace("jar:file:///", "jar:file:/"), uri.toString());
    }
    assertThrows(FileSystemNotFoundException.class, () -> Path.of(uri));
  }

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
  public void testCopyRecursivelyCpToJimUsingCloseablePath() throws Exception {
    String classFile = LoggerFactory.class.getSimpleName() + ".class";
    URI uri = LoggerFactory.class.getResource(classFile).toURI();
    LOGGER.info("URI: {}.", uri);
    assertThrows(FileSystemNotFoundException.class, () -> Path.of(uri));
    try (CloseablePath p = PathUtils.fromUri(uri).path()) {
      Path root = p.getParent().getParent();
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
